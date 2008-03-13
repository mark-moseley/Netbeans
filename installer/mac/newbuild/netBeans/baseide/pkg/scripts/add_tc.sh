#!/bin/sh -x
nb_dir=$1
tc_dir=$2

echo Changing netbeans.conf in $nb_dir
echo Tomcat is in $tc_dir

if [ "$nb_dir" = "" ] || [ "$tc_dir" = "" ]
then
  exit
fi
if [ -d "$nb_dir" ] && [ -d "$tc_dir" ]
then
  cd "$nb_dir" 
  cd Contents/Resources/NetBeans*/etc
  if [ -f netbeans.conf ]
  then
    token=`date "+%Y%m%d%H%M%S"`
    echo netbeans.conf found: `pwd`/netbeans.conf
    cp netbeans.conf netbeans.conf_orig_tc
    cat netbeans.conf_orig_tc  | sed -e 's|netbeans_default_options=\"|netbeans_default_options=\"-J-Dorg.netbeans.modules.tomcat.autoregister.catalinaHome='$tc_dir' -J-Dorg.netbeans.modules.tomcat.autoregister.token='$token' |' > netbeans.conf
  else
    echo No netbeans.conf in: `pwd`
  fi
fi

