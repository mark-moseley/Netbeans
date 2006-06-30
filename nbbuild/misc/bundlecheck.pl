#!/usr/bin/env perl
# -*- perl -*-
# The contents of this file are subject to the terms of the Common Development
# and Distribution License (the License). You may not use this file except in
# compliance with the License.
#
# You can obtain a copy of the License at http://www.netbeans.org/cddl.html
# or http://www.netbeans.org/cddl.txt.
#
# When distributing Covered Code, include this CDDL Header Notice in each file
# and include the License file at http://www.netbeans.org/cddl.txt.
# If applicable, add the following below the CDDL Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
# Microsystems, Inc. All Rights Reserved.

require 5.005;
use File::Find;

my @files = ();
my $quiet = 0;

if ($#ARGV < 0) {
    die "usage: $0 [-q] file | directory ...\n";
}

if ($ARGV[0] eq "-q") {
    $quiet = 1;
    shift @ARGV;
}

foreach my $name (@ARGV) {
    if (-f $name) {
	push @files, $name;
    } elsif (-d $name) {
        find(sub {
                 if (-f && m,\.java$, || -f && m,\bBundle.properties$,) {
                     push @files, $File::Find::name;
                 }
             },
             $name);
    }
}

#
# read Bundle.properties
#

@props = ();

foreach $f (@files) {
    next if $f !~ m,\bBundle.properties$,i;

    print STDERR "*** $f\n" unless $quiet;
    
    open FH, "< $f" or die;
    {
        local $/ = undef;
        $all = <FH>;
    }
    close FH;

    @lines = split /\r\n|\n|\r/, $all;
    for ($lineno = 0; $lineno <= $#lines; $lineno++) {
	$_ = $lines[$lineno];
        
        next if /^\s*#/;
        next if /^\s*$/;

        if (m,^([^=]+)=(.*)$,) {
            $k = $1;
            $k =~ s,\\ , ,g;
            push @props, { key => $k,
                           file => $f,
                           lineno => $lineno + 1,
                           line => $_,
                           used => 0
                         };
        }

        while (m,\\$, && $lineno <= $#lines) {
            $lineno++;
            $_ = $lines[$lineno];
        }
    }
}

#
# go through *.java
#
  
  
foreach $f (@files) {
    next if $f =~ m,\bBundle.properties$,i;

    print STDERR "*** $f\n" unless $quiet;
    
    open FH, "< $f" or die;
    {
        local $/ = undef;
        $all = <FH>;
    }
    close FH;

    foreach $p (@props) {
        next if $p->{used} > 0;
        $pat = $p->{key};
        $pat = quotemeta $pat;
        $p->{used}++ if $all =~ m,\"$pat\",;
    }
}

foreach $p (@props) {
    next if $p->{line} =~ m!/!; # probably a filename localization, not in Java code
    next if $p->{line} =~ m!^OpenIDE-Module-!; # manifest localization, not in Java code
    print "$p->{file}:$p->{lineno}: $p->{line}\n" if $p->{used} == 0;
}
