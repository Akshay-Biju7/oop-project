@echo off
set DIR=%~dp0
call "%DIR%.tools\maven\bin\mvn.cmd" %*
