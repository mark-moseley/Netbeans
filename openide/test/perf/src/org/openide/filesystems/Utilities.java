/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.filesystems;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * A set of utility methods.
 */
public abstract class Utilities {
    
    /** Counts padding size from given size, i.e. 1 for less than 10, 3 for 100 - 999, etc. */
    public static int expPaddingSize(int size) {
        int ret = 0;
        
        while (size > 0) {
            size /= 10;
            ret++;
        }
        return ret;
    }
    
    /** Appends paddingSize number of digits e.g. 00digit */
    public static void appendNDigits(int digit, int paddingSize, StringBuffer buffer) {
        int localLength = paddingSize - 1;
        int exp[] = new int[] { 0, 10, 100, 1000, 10000, 100000, 1000000 };

        while (digit < exp[localLength--]) {
            buffer.append('0');
        }

        buffer.append(String.valueOf(digit));
    }
    
    /** Creates jar file 
     * @param srcdir which folder to be zipped
     * @param <tt>name</tt> name of the jar
     */
    public static File createJar(File srcdir, String name) throws Exception {
        Process proc = Runtime.getRuntime().exec("jar cf " + name + " .", null, srcdir);
        proc.waitFor();
        copyIS(proc.getErrorStream(), System.out);
        
        return new File(srcdir, name);
    }
    
    /** Copy content of a stream to a PrintStream */
    public static void copyIS(InputStream is, PrintStream out) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String str;
        while ((str = reader.readLine()) != null) {
            out.println(str);
        }
    }

    /**
     * Simple grep
     */
    public static final class Matcher {
        private State first;

        /** new Matcher */
        public Matcher(String[] patterns) {
            first = new State(null);
            initStates(patterns);
        }

        /** Inits this Matcher */
        private void initStates(String[] patterns) {
            List groups = createGroups(patterns, 0);
            StringGroup.resolveGroups(groups);
            StringGroup.fillState(first, groups);
            createDG(first);
        }

        /** Creates directed graph of States */
        private static void createDG(State state) {
            Transition[] ts = state.getTransitions();
            for (int i = 0; i < ts.length; i++) {
                createDG(ts[i].getState(), ts);
            }
        }

        /** Creates directed cycled graph of States*/
        private static void createDG(State state, Transition[] ts) {
            Transition[] myts = state.getTransitions();

            for (int j = 0; j < myts.length; j++) {
                createDG(myts[j].getState(), ts);
            }

            mergeTransitions(state, ts);
        }

        /** Creates cycles in a graph of States
         * Adds given Transitions to given state.
         */
        private static void mergeTransitions(State state, Transition[] ts) {
            for (int i = 0; i < ts.length; i++) {
                State istate = ts[i].getState();
                if (istate == state) {
                    continue;
                }

                if (state.isDefined(ts[i].getChar())) {
                    State tmp = state.getNext(ts[i].getChar());
                    if (tmp != istate) {
                        makePtr(tmp, istate);
                    }
                } else {
                    state.addTransition(ts[i]);
                }
            }
        }

        /** Links one state to another state */
        private static void makePtr(State from, State to) {
            Transition[] tots = to.getTransitions();
            mergeTransitions(from, tots);

            if (to.isTerminal()) {
                String[] matches = to.getMatches();
                from.markAsTerminal(matches[0]);
                for (int i = 1; i < matches.length; i++) {
                    from.addMatch(matches[i]);
                }
            }
        }

        /** Creates groups for patterns */
        static List createGroups(String[] patterns, int level) {
            HashMap map = new HashMap();

            for (int i = 0; i < patterns.length; i++) {
                Character divider = new Character(patterns[i].charAt(level));
                StringGroup grp = (StringGroup) map.get(divider);
                if (grp == null) {
                    grp = new StringGroup(level);
                    map.put(divider, grp);
                }
                grp.addString(patterns[i]);
            }

            return new ArrayList(map.values());
        }

        /** @return initial state for this Matcher */
        public State getInitState() {
            return first;
        }

        /** Encapsulates a group of strings with a common prefix */
        static final class StringGroup {
            private List strings;
            private final int startIndex;
            private List subGroups;
            private int minIdx;
            private int endIndex;
            private boolean terminal;

            /** New group */
            public StringGroup(int idx) {
                this.startIndex = idx;
                minIdx = Integer.MAX_VALUE;
                strings = new ArrayList();
                this.terminal = false;
            }

            /** Adds a String to this group */
            public void addString(String s) {
                strings.add(s);
                minIdx = Math.min(minIdx, s.length());
            }

            /** @return true if this group contains only one String */
            private boolean isTerminal() {
                if (strings.size() <= 1 || terminal) {
                    return true;
                } else {
                    return false;
                }
            }

            private boolean hasSubGroups() {
                return (subGroups != null) && (subGroups.size() > 0);
            }

            /** @return first char for this group */
            private char getFirstChar() {
                return ((String) strings.get(0)).charAt(startIndex);
            }

            /** Resolves this group and all its sub groups */
            private void resolve() {
                if (isTerminal()) {
                    endIndex = minIdx;
                    return;
                }

                subGroups = new ArrayList();
                int i = startIndex + 1;
out:            for (; i < minIdx; i++) {
                    char c = ((String) strings.get(0)).charAt(i);

                    for (int j = 1; j < strings.size(); j++) {
                        char c2 = ((String) strings.get(j)).charAt(i);
                        if (c2 != c) {
                            String[] arr = (String[]) strings.toArray(new String[strings.size()]);
                            subGroups = createGroups(arr, endIndex = i);
                            break out;
                        }
                    }
                }

                if (i == minIdx) {
                    endIndex = minIdx;
                    List longStrings = new ArrayList();
                    for (int j = 0; j < strings.size(); j++) {
                        String str = (String) strings.get(j);
                        if (str.length() > minIdx) {
                            longStrings.add(str);
                        }
                    }

                    String[] arr = (String[]) longStrings.toArray(new String[longStrings.size()]);
                    subGroups = createGroups(arr, minIdx);
                    terminal = true;
                }

                resolveGroups(subGroups);
            }

            /** @return a String that */
            private String getShortOrAny() {
                for (int j = 0; j < strings.size(); j++) {
                    String str = (String) strings.get(j);
                    if (str.length() == minIdx) {
                        return str;
                    }
                }

                return (String) strings.get(0);
            }

            /** @return initial State for this group */
            private State createEntryState(State firstState) {
                State entry = new State(firstState);
                String any = getShortOrAny();
                State iter = entry;

                for (int i = startIndex + 1; i < endIndex; i++) {
                    char c = any.charAt(i);
                    State state = new State(firstState);
                    Transition t = new Transition(c, state);
                    iter.addTransition(t);
                    iter = state;
                }

                if (isTerminal()) {
                    iter.markAsTerminal(any);
                } 

                if (hasSubGroups()) {
                    fillState(iter, firstState, subGroups);
                }

                return entry;
            }

            /** Joins given groups to state with a given firstState */
            public static void fillState(State state, List groups) {
                fillState(state, state, groups);
            }

            /** Joins given groups to state with a given firstState */
            private static void fillState(State state, State firstState, List groups) {
                Transition[] tmp = new Transition[1];
                for (int i = 0; i < groups.size(); i++) {
                    StringGroup grp = (StringGroup) groups.get(i);
                    State xstate = grp.createEntryState(firstState);
                    tmp[0] = new Transition(grp.getFirstChar(), xstate);
                    mergeTransitions(state, tmp);
                }
            }

            /** Resolves all groups */
            static void resolveGroups(List groups) {
                int len = groups.size();

                for (int i = 0; i < len; i++) {
                    StringGroup grp = (StringGroup) groups.get(i);
                    grp.resolve();
                }
            }
        }

        /** Represents state of the automata */
        public static final class State {
            private HashMap transitions;
            private State first;
            private boolean terminal;
            private List matches;

            /** New State */
            public State(State first) {
                this.transitions = new HashMap();
                this.matches = new ArrayList();
                this.terminal = false;
                if (first == null) {
                    this.first = this;
                } else {
                    this.first = first;
                }
            }

            /** Adds on Transition */
            void addTransition(Transition t) {
                transitions.put(t.getHashKey(), t);
            }

            /** @return next State for the given char */
            public State getNext(char c) {
                Transition t = (Transition) transitions.get(new Character(c));
                if (t != null) {
                    return t.getState();
                } else {
                    return first;
                }
            }

            /** @return true iff there exists a Transition for given char */
            boolean isDefined(char c) {
                return (transitions.get(new Character(c)) != null);
            }

            /** This is a match */
            void markAsTerminal(String match) {
                terminal = true;
                addMatch(match);
            }

            /** Adds this String to its matches */
            void addMatch(String match) {
                matches.add(match);
            }

            /** @return matches */
            public String[] getMatches() {
                return (String[]) matches.toArray(new String[matches.size()]);
            }

            /** @return true iff it is a match */
            public boolean isTerminal() {
                return terminal;
            }

            /** @return all transitions for this State */
            Transition[] getTransitions() {
                return (Transition[]) transitions.values().toArray(new Transition[transitions.size()]);
            }
        }

        /** Bound to State - represents mapping of a char to next State */
        static final class Transition {
            private char c; 
            private State nextState;

            /** New Transition */
            public Transition(char c, State nextState) {
                this.c = c;
                this.nextState = nextState;
            }

            /** Hash key for this Transition */
            public Object getHashKey() {
                return new Character(c);
            }

            /** @return char */
            public char getChar() {
                return c;
            }

            /** @return State for this Transition */
            public State getState() {
                return nextState;
            }
        }
    }
    
}
