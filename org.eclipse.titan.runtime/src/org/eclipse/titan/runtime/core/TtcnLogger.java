/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.Stack;

/**
 * originally TTCN_Logger
 * @author Arpad Lovassy
 * @author Andrea Palfi
 */
public final class TtcnLogger {

	public static enum matching_verbosity_t { VERBOSITY_COMPACT, VERBOSITY_FULL };

	public static enum Severity {
		NOTHING_TO_LOG,
		ACTION_UNQUALIFIED,
		DEFAULTOP_ACTIVATE,
		DEFAULTOP_DEACTIVATE,
		DEFAULTOP_EXIT,
		DEFAULTOP_UNQUALIFIED,
		ERROR_UNQUALIFIED,
		EXECUTOR_RUNTIME,
		EXECUTOR_CONFIGDATA,
		EXECUTOR_EXTCOMMAND,
		EXECUTOR_COMPONENT,
		EXECUTOR_LOGOPTIONS,
		EXECUTOR_UNQUALIFIED,
		FUNCTION_RND,
		FUNCTION_UNQUALIFIED,
		PARALLEL_PTC,
		PARALLEL_PORTCONN,
		PARALLEL_PORTMAP,
		PARALLEL_UNQUALIFIED,
		TESTCASE_START,
		TESTCASE_FINISH,
		TESTCASE_UNQUALIFIED,
		PORTEVENT_PQUEUE,
		PORTEVENT_MQUEUE,
		PORTEVENT_STATE,
		PORTEVENT_PMIN,
		PORTEVENT_PMOUT,
		PORTEVENT_PCIN,
		PORTEVENT_PCOUT,
		PORTEVENT_MMRECV,
		PORTEVENT_MMSEND,
		PORTEVENT_MCRECV,
		PORTEVENT_MCSEND,
		PORTEVENT_DUALRECV,
		PORTEVENT_DUALSEND,
		PORTEVENT_UNQUALIFIED,
		PORTEVENT_SETSTATE,
		STATISTICS_VERDICT,
		STATISTICS_UNQUALIFIED,
		TIMEROP_READ,
		TIMEROP_START,
		TIMEROP_GUARD,
		TIMEROP_STOP,
		TIMEROP_TIMEOUT,
		TIMEROP_UNQUALIFIED,
		USER_UNQUALIFIED,
		VERDICTOP_GETVERDICT,
		VERDICTOP_SETVERDICT,
		VERDICTOP_FINAL,
		VERDICTOP_UNQUALIFIED,
		WARNING_UNQUALIFIED,
		// MATCHING and DEBUG should be at the end (not included in LOG_ALL)
		MATCHING_DONE,
		MATCHING_TIMEOUT,
		MATCHING_PCSUCCESS,
		MATCHING_PCUNSUCC,
		MATCHING_PMSUCCESS,
		MATCHING_PMUNSUCC,
		MATCHING_MCSUCCESS,
		MATCHING_MCUNSUCC,
		MATCHING_MMSUCCESS,
		MATCHING_MMUNSUCC,
		MATCHING_PROBLEM,
		MATCHING_UNQUALIFIED,
		DEBUG_ENCDEC,
		DEBUG_TESTPORT,
		DEBUG_USER,
		DEBUG_FRAMEWORK,
		DEBUG_UNQUALIFIED,
		NUMBER_OF_LOGSEVERITIES, // must follow the last individual severity
		LOG_ALL_IMPORTANT
	};

	public static final Severity sev_categories[]=
	{
		Severity.NOTHING_TO_LOG,//=0
		Severity.ACTION_UNQUALIFIED,
		Severity.DEFAULTOP_UNQUALIFIED,
		Severity.ERROR_UNQUALIFIED,
		Severity.EXECUTOR_UNQUALIFIED,
		Severity.FUNCTION_UNQUALIFIED,
		Severity.PARALLEL_UNQUALIFIED,
		Severity.TESTCASE_UNQUALIFIED,
		Severity.PORTEVENT_UNQUALIFIED,
		Severity.STATISTICS_UNQUALIFIED,
		Severity.TIMEROP_UNQUALIFIED,
		Severity.USER_UNQUALIFIED,
		Severity.VERDICTOP_UNQUALIFIED,
		Severity.WARNING_UNQUALIFIED,
		Severity.MATCHING_UNQUALIFIED,
		Severity.DEBUG_UNQUALIFIED,
	};

	public static String severity_category_names[]={
		"NULL",
		"ACTION",
		"DEFAULTOP",
		"ERROR",
		"EXECUTOR",
		"FUNCTION",
		"PARALLEL",
		"TESTCASE",
		"PORTEVENT",
		"STATISTICS",
		"TIMEROP",
		"USER",
		"VERDICTOP",
		"WARNING",
		"MATCHING",
		"DEBUG",
	};

	private static class log_event_struct {
		StringBuilder buffer;
		Severity severity;
		//event_destination, etc...
	}

	String logMatchBuffer = null;
	int logMatchBufferLen=0;
	int logMachBufferSize=0;
	boolean logMachPrinted = false;

	private static log_event_struct current_event = null;
	private static Stack<log_event_struct> events = new Stack<TtcnLogger.log_event_struct>();


	public static void initialize_logger() {
		//empty for the time being
	}

	public static void terminate_logger() {
		//empty for now
	}

	public static void log(final Severity msg_severity, final String formatString, final Object... args ) {
		log_va_list(msg_severity, formatString, args);
	}

	public static void log_str(final Severity msg_severity, final String string ) {
		log_line(msg_severity, string == null ? "<NULL pointer>": string);
	}

	public static void log_va_list(final Severity msg_severity, final String formatString, final Object... args) {
		log_line(msg_severity, String.format(formatString, args));
	}

	public static void begin_event(final Severity msg_severity) {
		current_event = new log_event_struct();
		current_event.severity = msg_severity;
		current_event.buffer = new StringBuilder();
		events.push(current_event);
	}

	public static void begin_event_log2str() {
		begin_event(Severity.USER_UNQUALIFIED);//and true
	}

	public static void end_event() {
		if (current_event != null) {
			log_line(current_event.severity, current_event.buffer.toString());

			events.pop();
			if (!events.isEmpty()) {
				current_event = events.peek();
			} else {
				current_event = null;
			}
		}
	}

	public static TitanCharString end_event_log2str() {
		if (current_event != null) {
			final TitanCharString ret_val = new TitanCharString(current_event.buffer);

			events.pop();
			if (!events.isEmpty()) {
				current_event = events.peek();
			} else {
				current_event = null;
			}

			return ret_val;
		}

		return new TitanCharString();
	}

	private static void log_line(final Severity event_severity, final String message) {
		long timestamp = System.currentTimeMillis();
		final long milisec = timestamp % 1000;
		timestamp = timestamp / 1000;
		final long secs = timestamp % 60;
		timestamp = timestamp / 60;
		final long minutes = timestamp % 60;
		timestamp = timestamp / 60;
		final long hours = timestamp % 24;
		timestamp = timestamp / 24;
		final String timestampString = String.format("%02d:%02d:%02d.%03d", hours, minutes, secs, milisec);

		System.out.println("Logger says: " + timestampString + " "+ message);
	}

	public static void finish_event() {
		if (current_event != null) {
			log_event_str("<unfinished>");
			end_event();
		}
	}

	public static void log_event( final String formatString, final Object... args ) {
		log_event_va_list(formatString, args);
	}

	public static void log_event_str( final String string ) {
		if (current_event != null) {
			current_event.buffer.append(string);
		}
	}

	public static void log_event_va_list(final String formatString, final Object... args) {
		if (current_event != null) {
			current_event.buffer.append(String.format(formatString, args));
		}
	}

	public static void log_char( final char c ) {
		// TODO Auto-generated method stub
	}

	public static void log_event_uninitialized() {
		log_event_str("<uninitialized template>");
	}

	public static void log_hex( final byte aHexDigit ) {
		// TODO Auto-generated method stub
	}

	public static void log_event_unbound() {
		//TODO a bit more complicated
		log_event_str("<unbound>");
	}

	public static void log_octet( final char aOctet ) {
		// TODO Auto-generated method stub
	}

	public static void log_bit( final byte aBit ) {
		// TODO Auto-generated method stub
	}

	public static matching_verbosity_t get_matching_verbosity() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void print_logmatch_buffer() {
		// TODO Auto-generated method stub
	}

	public static void log_logmatch_info(final String string) {
		// TODO Auto-generated method stub
	}

	public static void set_logmatch_buffer_len(final int previous_size) {
		// TODO Auto-generated method stub
	}

	public static int get_logmatch_buffer_len() {
		// TODO Auto-generated method stub
		return 0;
	}
}
