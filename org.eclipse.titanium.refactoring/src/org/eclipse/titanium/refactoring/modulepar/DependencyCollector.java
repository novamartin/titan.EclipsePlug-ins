/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.modulepar;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Port;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Timer;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FriendModule;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ImportModule;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;

/**
 * This class is only instantiated by the {@link ExtractModuleParRefactoring} once per
 * each refactoring operation. The method {@link #readDependencies()} returns
 * a {@link WorkspaceJob} which collects all dependencies. After running the job,
 * the <code>copyMap</code> contains all the pieces of code from the files of the
 * source project, which are going to be copied to the new project.
 * 
 * @author Viktor Varga
 */
public class DependencyCollector {
	private static final String MODULE_HEADER = "\n//Generated by Titanium->Extract modulepar\n\nmodule {0} \n'{'\n";
	private static final String MODULE_TRAILER = "\n\n}\n";
	private static final String DOUBLE_SEPARATOR = "\n\n";
	private static final String SINGLE_SEPARATOR = "\n";
	
	private final IProject sourceProj;
	private final ProjectSourceParser projectSourceParser;
	
	private final Set<Def_ModulePar> selection;
	/** parts of files to copy */
	private Map<IPath, StringBuilder> copyMap;
	/** files to copy completely */
	private List<IFile> filesToCopy;
	
	DependencyCollector(Set<Def_ModulePar> selection, IProject sourceProj) {
		this.selection = selection;
		this.sourceProj = sourceProj;
		this.projectSourceParser = GlobalParser.getProjectSourceParser(sourceProj);
	}
	
	Map<IPath, StringBuilder> getCopyMap() {
		return copyMap;
	}
	List<IFile> getFilesToCopy() {
		return filesToCopy;
	}
	
	WorkspaceJob readDependencies() {
		WorkspaceJob job = new WorkspaceJob("ExtractModulePar: reading dependencies from source project") {

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(sourceProj);
				//find all dependencies of the 'selection' definition
				Set<IResource> allFiles = new HashSet<IResource>();
				Set<IResource> asnFiles = new HashSet<IResource>();
				NavigableSet<ILocateableNode> dependencies = new TreeSet<ILocateableNode>(new LocationComparator());
				for (Def_ModulePar def: selection) {
					/*
					 * Def_ModulePars with mutliple entries in a single modulepar block have incorrect location info
					 *  (all entries have a location info equal to the location of their parent block)
					 *  that is why the dependencies must be collected into a set that is not sorted by location
					 *  
					 *  TODO fix grammar definitions
					 * */
					Set<ILocateableNode> nonSortedTempSet = new HashSet<ILocateableNode>();
					collectDependencies(def, nonSortedTempSet, allFiles, asnFiles);
					dependencies.addAll(nonSortedTempSet);
					allFiles.add(def.getLocation().getFile());
					//adding the selection itself to the dependencies
					if (!dependencies.contains(def)) {
						dependencies.add(def);
					}
					//get imports and friends for all files
					for (IResource r: allFiles) {
						if (!(r instanceof IFile)) {
							continue;
						}
						IFile f = (IFile)r;
						Module m = projectSourceParser.containedModule(f);
						ImportFinderVisitor impVisitor = new ImportFinderVisitor();
						m.accept(impVisitor);
						List<ImportModule> impDefs = impVisitor.getImportDefs();
						List<FriendModule> friendDefs = impVisitor.getFriendDefs();
						filterImportDefinitions(allFiles, impDefs, projectSourceParser);
						filterFriendDefinitions(allFiles, friendDefs, projectSourceParser);
						dependencies.addAll(impDefs);
						dependencies.addAll(friendDefs);
						//the dependencies are sorted by file & location to avoid reading through a file multiple times
					}
				}
				//collect the text to insert into the new project
				copyMap = new HashMap<IPath, StringBuilder>();
				try {
					InputStream is = null;
					InputStreamReader isr = null;
					IResource lastFile = null;
					IResource currFile;
					ILocateableNode lastD = null;
					int currOffset = 0;
					for (ILocateableNode d: dependencies) {
						currFile = d.getLocation().getFile();
						if (!(currFile instanceof IFile)) {
							ErrorReporter.logError("ExtractModulePar/DependencyCollector: IResource `" + currFile.getName() + "' is not an IFile.");
							continue;
						}
						if (currFile != lastFile) {
							//started reading new file
							lastD = null;
							if (lastFile != null) {
								addToCopyMap(lastFile.getProjectRelativePath(), MODULE_TRAILER);
							}
							if (isr != null) {
								isr.close();
							}
							if (is != null) {
								is.close();
							}
							is = ((IFile)currFile).getContents();
							isr = new InputStreamReader(is);
							currOffset = 0;
							Module m = projectSourceParser.containedModule((IFile)currFile);
							String moduleName = m.getIdentifier().getTtcnName();
							addToCopyMap(currFile.getProjectRelativePath(), MessageFormat.format(MODULE_HEADER, moduleName));
						}
						int toSkip = getNodeOffset(d)-currOffset;
						if (toSkip < 0) {
							reportOverlappingError(lastD, d);
							continue;
						}
						isr.skip(toSkip);
						//separators
						if (d instanceof ImportModule && lastD instanceof ImportModule) {
							addToCopyMap(currFile.getProjectRelativePath(), SINGLE_SEPARATOR);
						} else if (d instanceof FriendModule && lastD instanceof FriendModule) {
							addToCopyMap(currFile.getProjectRelativePath(), SINGLE_SEPARATOR);
						} else {
							addToCopyMap(currFile.getProjectRelativePath(), DOUBLE_SEPARATOR);
						}
						//
						insertDependency(d, currFile, isr);
						currOffset = d.getLocation().getEndOffset();
						lastFile = currFile;
						lastD = d;
					}
					if (lastFile != null) {
						addToCopyMap(lastFile.getProjectRelativePath(), MODULE_TRAILER);
					}
					if (isr != null) {
						isr.close();
					}
					if (is != null) {
						is.close();
					}
					//copy the full content of asn files without opening them
					filesToCopy = new ArrayList<IFile>();
					for (IResource r: asnFiles) {
						if (!(r instanceof IFile)) {
							ErrorReporter.logError("ExtractModulePar/DependencyCollector: IResource `" + r.getName() + "' is not an IFile.");
							continue;
						}
						filesToCopy.add((IFile)r);
					}
				} catch (IOException ioe) {
					ErrorReporter.logError("ExtractModulePar/DependencyCollector: Error while reading source project.");
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
			
			private void insertDependency(ILocateableNode d, IResource res, InputStreamReader isr) throws IOException {
				char[] content = new char[d.getLocation().getEndOffset()-getNodeOffset(d)];
				isr.read(content, 0, content.length);
				addToCopyMap(res.getProjectRelativePath(), new String(content));
			}
			
		};
		job.setUser(true);
		job.schedule();
		return job;
	}

	private void collectDependencies(Definition start, Set<ILocateableNode> allDefs, Set<IResource> imports, Set<IResource> asnFiles) {
		Set<Definition> currDefs;
		Set<Definition> prevDefs = new HashSet<Definition>();
		prevDefs.add(start);
		//TODO containsAll(): is this ok for sure?
		while (!(allDefs.containsAll(prevDefs))) {
			currDefs = new HashSet<Definition>();
			allDefs.addAll(prevDefs);
			for (Definition d: prevDefs) {
				DependencyFinderVisitor vis = new DependencyFinderVisitor(currDefs, imports, asnFiles);
				d.accept(vis);
			}
			prevDefs = currDefs;
		}
	}
	
	/**
	 * Compares {@link ILocateableNode}s by comparing the file paths as strings.
	 * If the paths are equal, the two offset integers are compared.
	 * */
	private class LocationComparator implements Comparator<ILocateableNode> {

		@Override
		public int compare(ILocateableNode arg0, ILocateableNode arg1) {
			
			IResource f0 = arg0.getLocation().getFile();
			IResource f1 = arg1.getLocation().getFile();
			if (!f0.equals(f1)) {
				return f0.getFullPath().toString().compareTo(f1.getFullPath().toString());
			}
			int o0 = getNodeOffset(arg0);
			int o1 = getNodeOffset(arg1);
			return (o0 < o1) ? -1 : ((o0 == o1) ? 0 : 1);//TODO update with Java 1.7 to Integer.compare
		}
		
	}

	/**
	 * Collects all the definitions that are referenced from inside a single (the visited) definition.
	 * */
	class DependencyFinderVisitor extends ASTVisitor {

		private final Set<Definition> definitions;
		private final Set<IResource> imports;
		private final Set<IResource> asnFiles;
		
		DependencyFinderVisitor(Set<Definition> definitions, Set<IResource> imports, Set<IResource> asnFiles) {
			this.definitions = definitions;
			this.imports = imports;
			this.asnFiles = asnFiles;
		}
		
		@Override
		public int visit(IVisitableNode node) {
			//when finding a reference, check if the referred definition has already been found:
			//if no, then start a new visitor on the definition itself
			if (node instanceof Undefined_LowerIdentifier_Value) {
				((Undefined_LowerIdentifier_Value)node).getAsReference();
				return V_CONTINUE;
			}
			if (node instanceof Reference) {
				Reference ref = (Reference)node;
				Assignment as = ref.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
				if (as == null) {
					return V_CONTINUE;
				}
				IResource asFile = as.getLocation().getFile();
				if ("asn".equals(asFile.getFileExtension())) {
					//copy .asn files completely
					imports.add(asFile);
					asnFiles.add(asFile);
					collectAllASNs(asFile);
				} else if (as instanceof Definition) {
					//only for .ttcn files
					Definition def = (Definition)as;
					if (!definitions.contains(def)) {
						if (isGoodType(def)) {
							definitions.add(def);
							IResource refLoc = ref.getLocation().getFile();
							if (asFile != refLoc) {
								imports.add(asFile);
							}
						}
					}
				}
				return V_CONTINUE;
			}
			
			return V_CONTINUE;
		}
		
		private boolean isGoodType(final Definition definition) {
			if (definition instanceof Def_Var
					|| definition instanceof Def_Var_Template
					|| definition instanceof FormalParameter
					|| definition instanceof Def_Timer
					|| definition instanceof Def_Port) {
				return false;
			}
			return !definition.isLocal();
		}
		
		/**
		 * Recursively collects all ASN files through the import statements of the ASN file given as argument.
		 * */
		private void collectAllASNs(IResource asnBaseFile) {
			if (asnBaseFile instanceof IFile) {
				IFile f = (IFile)asnBaseFile;
				Module mod = projectSourceParser.containedModule(f);
				List<Module> impMods = mod.getImportedModules();
				for (Module m: impMods) {
					IResource impModRes = m.getLocation().getFile();
					if (asnFiles.contains(impModRes)) {
						continue;
					}
					asnFiles.add(impModRes);
					collectAllASNs(impModRes);
				}
			}
		}
		
	}
	
	/**
	 * Searches for import & friend definitions in a {@link Module}.
	 * */
	static class ImportFinderVisitor extends ASTVisitor {
		
		private final List<ImportModule> importDefs;
		private final List<FriendModule> friendDefs;
		
		ImportFinderVisitor() {
			importDefs = new ArrayList<ImportModule>();
			friendDefs = new ArrayList<FriendModule>();
		}
		
		List<ImportModule> getImportDefs() {
			return importDefs;
		}
		List<FriendModule> getFriendDefs() {
			return friendDefs;
		}
		
		@Override
		public int visit(IVisitableNode node) {
			if (node instanceof ImportModule) {
				importDefs.add((ImportModule)node);
				return V_SKIP;
			} else if (node instanceof FriendModule) {
				friendDefs.add((FriendModule)node);
				return V_SKIP;
			}
			return V_CONTINUE;
		}
		
	}


	/** 
	 * Returns the <code>importDefs</code> list, with the {@link ImportModule}s removed which refer to
	 * modules that are not contained in the <code>allFiles</code> set.
	 * */
	private static void filterImportDefinitions(Set<IResource> allFiles, List<ImportModule> importDefs, ProjectSourceParser projParser) {
		List<Identifier> allFileIds = new ArrayList<Identifier>(allFiles.size());
		for (IResource r: allFiles) {
			if (!(r instanceof IFile)) {
				continue;
			}
			IFile f = (IFile)r;
			Module m = projParser.containedModule(f);
			allFileIds.add(m.getIdentifier());
		}
		ListIterator<ImportModule> importIt = importDefs.listIterator();
		while (importIt.hasNext()) {
			ImportModule im = importIt.next();
			Identifier id = im.getIdentifier();
			if (!allFileIds.contains(id)) {
				importIt.remove();
			}
		}
	}
	/**
	 * Returns the <code>friendDefs</code> list, with the {@link FriendModule}s removed which refer to
	 * modules that are not contained in the <code>allFiles</code> set.
	 * */
	private static void filterFriendDefinitions(Set<IResource> allFiles, List<FriendModule> friendDefs, ProjectSourceParser projParser) {
		List<Identifier> allFileIds = new ArrayList<Identifier>(allFiles.size());
		for (IResource r: allFiles) {
			if (!(r instanceof IFile)) {
				continue;
			}
			IFile f = (IFile)r;
			Module m = projParser.containedModule(f);
			allFileIds.add(m.getIdentifier());
		}
		ListIterator<FriendModule> importIt = friendDefs.listIterator();
		while (importIt.hasNext()) {
			FriendModule fm = importIt.next();
			Identifier id = fm.getIdentifier();
			if (!allFileIds.contains(id)) {
				importIt.remove();
			}
		}
	}

	private void addToCopyMap(IPath relativePath, String toAdd) {
		if (copyMap.containsKey(relativePath)) {
			StringBuilder sb = copyMap.get(relativePath);
			sb.append(toAdd);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(toAdd);
			copyMap.put(relativePath, sb);
		}
	}
	
	private int getNodeOffset(ILocateableNode node) {
		int o = node.getLocation().getOffset();
		if (ExtractModuleParRefactoring.ENABLE_COPY_COMMENTS && node instanceof Definition) {
			Location commentLoc = ((Definition)node).getCommentLocation();
			if (commentLoc != null) {
				o = commentLoc.getOffset();
			}
		}
		return o;
	}

	private void reportOverlappingError(ILocateableNode defCurrent, ILocateableNode defOverlapping) {
		Location dLocOverlap = defOverlapping == null ? null : defOverlapping.getLocation();
		Location dLocCurr = defCurrent == null ? null : defCurrent.getLocation();
		String idOverlap = null;
		if (defOverlapping instanceof Definition) {
			idOverlap = ((Definition)defOverlapping).getIdentifier().toString();
		} else if (defOverlapping instanceof ImportModule) {
			idOverlap = "ImportModule{" + ((ImportModule)defOverlapping).getIdentifier().toString() + "}";
		} else if (defOverlapping instanceof FriendModule) {
			idOverlap = "FriendModule{" + ((FriendModule)defOverlapping).getIdentifier().toString() + "}";
		}
		String idCurr = null;
		if (defCurrent instanceof Definition) {
			idCurr = ((Definition)defCurrent).getIdentifier().toString();
		} else if (defCurrent instanceof ImportModule) {
			idCurr = "ImportModule{" + ((ImportModule)defCurrent).getIdentifier().toString() + "}";
		} else if (defCurrent instanceof FriendModule) {
			idCurr = "FriendModule{" + ((FriendModule)defCurrent).getIdentifier().toString() + "}";
		}
		String msg1 = (dLocCurr == null) ? "null" :
				"Definition id: " + idCurr + 
				" (" + defCurrent.getClass().getSimpleName() + 
				") at " + dLocCurr.getFile() + ", offset " + dLocCurr.getOffset()
				+ "-" + dLocCurr.getEndOffset(); 
		String msg2 = (dLocOverlap == null) ? "null" : 
				"Definition id: " + idOverlap + 
				" (" + defOverlapping.getClass().getSimpleName() + 
				") at " + dLocOverlap.getFile() + ", offset " + dLocOverlap.getOffset()
				+ "-" + dLocOverlap.getEndOffset();
		ErrorReporter.logError("Warning! Locations overlap while reading source project: \n" + msg1
				 + "\n WITH \n" + msg2);
	}

}