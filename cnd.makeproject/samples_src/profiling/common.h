/*
 * Copyright (c) 2009, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#ifndef _COMMON_H
#define	_COMMON_H

#ifdef	__cplusplus
extern "C" {
#endif

#include <stdio.h>

// Tracing
typedef enum {
    msg_trace    = 1, // instruction-level tracing (e.g. trace each malloc, fopen)
    msg_explain  = 2, // work-level tracing (e.g. start/finish events for each work)
    msg_ref      = 4  // top-level tracing (e.g. expected cumulative effect of all works)
} msg_level_t;

extern int msg_levels;

#define PRINT(fmt, ...) printf(fmt, ##__VA_ARGS__);
#define REF(fmt, ...) if (msg_levels & msg_ref) { printf(fmt, ##__VA_ARGS__); }
#define TRACE(fmt, ...) if (msg_levels & msg_trace) { printf(fmt, ##__VA_ARGS__); }
#define EXPLAIN(fmt, ...) if (msg_levels & msg_explain) { printf(fmt, ##__VA_ARGS__); }
#define PAUSE(fmt, ...) if (msg_levels & msg_explain) { printf(fmt, ##__VA_ARGS__); getchar(); }
#define ERROR(fmt, ...) fprintf(stderr, fmt, ##__VA_ARGS__);

// CPU usage types
typedef enum {
    cpu_idle,
    cpu_usr,
    cpu_sys
} cpu_usage_t;

// Memory usage types
typedef enum {
    mem_none,
    mem_malloc,
    mem_mmap
} mem_usage_t;

typedef struct work {
    unsigned int id;        // work id
    cpu_usage_t cpu_usage;  // CPU usage type
    mem_usage_t mem_usage;  // memory usage type
    unsigned long mem_size; // memory size
} work_t ;

void work_explain(work_t* work);
void work_run(work_t* work, long micros);

long mem_usage(int threads, work_t* works);
int usrcpu_usage(int threads, work_t* works);
int syscpu_usage(int threads, work_t* works);

// Some convenience macros and functions
#define MICROS_PER_SECOND 1000000
#define MAX(a, b) ((a) < (b)? (b) : (a))

int cpucount();
void mem2str(char* buf, long bytes);

#ifdef	__cplusplus
}
#endif

#endif	/* _COMMON_H */
