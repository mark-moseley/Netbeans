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

#
# i18ncheck.pl - checks java source for internationalizable strings
#                not 100% foolproof !!
#
#
require 5.005;
use File::Find;

my $fixmode = 0;
my @files = ();
my @modifiedfiles = ();
my @lines = ();

if ($#ARGV < 0) {
    die "usage: $0 [-f] file | directory ...\n";
}

if ($ARGV[0] eq "-f") {
    $fixmode = 1;
    shift @ARGV;
}

if ($#ARGV < 0) {
    die "usage: $0 [-f] file | directory ...\n";
}

$SIG{'INT'} = sub {
    print_summary() if $fixmode;
    exit 255;
};

foreach my $name (@ARGV) {
    if (-f $name) {
	push @files, $name;
    } elsif (-d $name) {
        find(sub {
                 if (-f && m,\.java$,) {
                     push @files, $File::Find::name;
                 }
             },
             $name);
    }
}

foreach my $name (@files) {
    checkfile($name);
}

print_summary() if $fixmode;
exit 0;

#
# subroutines
#

sub checkfile {
    my $fname = shift;
    @lines = ();

    if (! open(FH, "< $fname")) {
	warn "cannot open file '$fname': $!";
	return;
    }
    @lines = <FH>;
    close FH;
    
    my $lineno = 1;
    my $modified = 0;
    
LOOP:
    while ($lineno <= $#lines) {
	$_ = $lines[$lineno - 1];
        
        if (m,/\*,,) {
            while ($lineno <= $#lines) {
                $_ = $lines[$lineno - 1];
                if (! m,\*/,) {
                    $lineno++;
                    next;
                } else {
                    last;
                }
            }
        }

        # skip line comment
        if (m,(^ *//.*$),) {
            $_ = $`;
        }
        
        if (checkline($_)) {
            if ($fixmode) {
                print "$fname:$lineno:\n";
                if (fixline($fname, $lineno)) {
                    $modified = 1;
                }
            } else {
                print "$fname:$lineno: $_";
            }
        }

        $lineno++;
    }


    if ($fixmode && $modified) {
        savefile($fname);
        push @modifiedfiles, $fname;
    }
}

sub fixline {
    my $fname = shift;
    my $lineno = shift;
    my $answer;
    
    print "\n";
    print "   " . $lines[$lineno - 4] if $lineno >= 4;
    print "   " . $lines[$lineno - 3] if $lineno >= 3;
    print "   " . $lines[$lineno - 2] if $lineno >= 2;
    print " =>" . $lines[$lineno - 1] if $lineno >= 1;
    print "   " . $lines[$lineno] if $lineno <= $#lines;;
    print "   " . $lines[$lineno + 1] if $lineno + 1 <= $#lines;;
    print "   " . $lines[$lineno + 2] if $lineno + 2 <= $#lines;;
    print "\n";
    
    do {
        print "** [M]ark this line with NOI18N -- [S]kip [m]: ";
        flush;
        $answer = <STDIN>;
        chomp $answer;
    } while (uc($answer) ne "M" && uc($answer) ne "S" && $answer ne "");

    if (uc($answer) eq "S") {
        return 0;
    } else {
        $lines[$lineno - 1] =~ s,\s*$,,;
        $lines[$lineno - 1] = $lines[$lineno - 1] . " // NOI18N\n";
        return 1;
    }
    
}

sub savefile {
    my $fname = shift;

    if (!rename $fname, "$fname.bak") {
        warn "** Cannot create backup for $fname, changes have not been saved\n";
        return;
    }

    if (! open(FH, "> $fname")) {
        warn "** Cannot save changes in $fname\n";
    }

    print FH @lines;
    close FH;
}

sub checkline {
    shift;

    return (! m,NOI18N,
            && ! m,getString\s*\(,
            && ! m,System\. ?err\. ?print,
            && ! m,getProperty,
            && ! m,getBoolean,
            && ! m,assert,
            && ! m,AssertionError,
            && ! m,/\*\*,
            && ! m,//.*",
            && ! m,get(Message|LocalizedFile),
            && ! m,\. ?(info|warning|fine|finer|finest)\s*\(,
            && ! m,\. ?log\s*\(\s*Level\s*\.\s*(SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST)\s*,
            && ! m,\@SuppressWarnings\s*\(,
            && ! m,new HelpCtx\s*\(,
            && ! m,new PropertyDescriptor\s*\(,
            && ! m,setIconBase\s*\(,
            && ! m,loadImage\s*\(,
            && ! m,getResource(AsStream)?\s*\(,
            && ! m,findResource ?\s*\(,
            && m,".+", 
           ); 
}

sub print_summary {
    if ($#modifiedfiles < 0) {
        print "\n\n** No file has been modified\n";
    } else {
        print "\n\n** The following files have been modified:\n\n";
        foreach my $fname (@modifiedfiles) {
            print "      $fname\n";
        }
    }
}
