#!/bin/bash

# installing parallel
mkdir ~/tmp ; pushd ~/tmp
wget ftp://ftp.gnu.org/gnu/parallel/parallel-latest.tar.bz2
tar xvjf parallel-latest.tar.bz2
pushd parallel-*
./configure && make && sudo make install
popd ; popd ; rm -rf ~/tmp
