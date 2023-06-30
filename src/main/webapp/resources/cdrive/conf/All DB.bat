
set dt=%date:~7,2%-%date:~4,2%-%date:~10,4%_%time:~0,2%_%time:~3,2%_%time:~6,2%

echo Backup database.....
echo %dt%
C:
cd C:\Program Files\MariaDB 10.5\bin 

echo Creating dir if not exist
if not exist "C:\webtis\databasebackup" mkdir C:\webtis\databasebackup

setlocal
set LogPath=C:\webtis\log\
set LogFileExt=.log
set LogFileName=Daily Backup%LogFileExt%
::use set MyLogFile=%date:~4% instead to remove the day of the week
::[COLOR="DarkRed"]set MyLogFile=%date%
::set MyLogFile=%MyLogFile:/=-%[/COLOR]
set MyLogFile=%MyLogFile%
set MyLogFile=%LogPath%%MyLogFile%%LogFileName%
::Note that the quotes are REQUIRED around %MyLogFIle% in case it contains a space
IF NOT Exist "%LogPath%" mkdir %LogPath%
If NOT Exist "%MyLogFile%" goto:noseparator
Echo.>>"%MyLogFile%"
Echo.========================================================================ITALIAWorks========================================>>"%MyLogFile%"
:noseparator
::echo.%Date% >>"%MyLogFile%"
::echo.%Time% >>"%MyLogFile%"
echo.%Date% %Time% Preparing for backup... >>"%MyLogFile%"
echo.%Date% %Time% starting backup... >>"%MyLogFile%"

mysqldump.exe -e -uroot -poctober181986* -hlocalhost webtis vrdata > C:\webtis\databasebackup\webtis_vrdata.sql
mysqldump.exe -e -uroot -poctober181986* -hlocalhost webtis cageowner > C:\webtis\databasebackup\webtis_cageowner.sql
mysqldump.exe -e -uroot -poctober181986* -hlocalhost webtis fishcagepayment > C:\webtis\databasebackup\webtis_fishcagepayment.sql

mysqldump.exe -e -uroot -poctober181986* -hlocalhost webtis > C:\webtis\databasebackup\webtis.sql
echo.%Date% %Time% Preparing to save the backup >>"%MyLogFile%"
echo.%Date% %Time% Location: C:\webtis\databasebackup >>"%MyLogFile%"
echo.%Date% %Time% Backup has been successfully proccessed with the file name of 'webtis_%dt%.sql' >>"%MyLogFile%"
echo.%Date% %Time% Location: C:\webtis\databasebackup\webtis_%dt%.sql >>"%MyLogFile%"

mysqldump.exe -e -uroot -poctober181986* -hlocalhost taxation > C:\webtis\databasebackup\taxation.sql
echo.%Date% %Time% Preparing to save the backup >>"%MyLogFile%"
echo.%Date% %Time% Location: C:\webtis\databasebackup >>"%MyLogFile%"
echo.%Date% %Time% Backup has been successfully proccessed with the file name of 'taxation_%dt%.sql' >>"%MyLogFile%"
echo.%Date% %Time% Location: C:\webtis\databasebackup\taxation_%dt%.sql >>"%MyLogFile%"

mysqldump.exe -e -uroot -poctober181986* -hlocalhost bank_cheque1 > C:\webtis\databasebackup\bank_cheque.sql
echo.%Date% %Time% Preparing to save the backup >>"%MyLogFile%"
echo.%Date% %Time% Location: C:\webtis\databasebackup >>"%MyLogFile%"
echo.%Date% %Time% Backup has been successfully proccessed with the file name of 'bank_cheque_%dt%.sql' >>"%MyLogFile%"
echo.%Date% %Time% Location: C:\webtis\databasebackup\bank_cheque_%dt%.sql >>"%MyLogFile%"

mysqldump.exe -e -uroot -poctober181986* -hlocalhost cashbook > C:\webtis\databasebackup\cashbook.sql
echo.%Date% %Time% Preparing to save the backup >>"%MyLogFile%"
echo.%Date% %Time% Location: C:\webtis\databasebackup >>"%MyLogFile%"
echo.%Date% %Time% Backup has been successfully proccessed with the file name of 'cashbook_%dt%.sql' >>"%MyLogFile%"
echo.%Date% %Time% Location: C:\webtis\databasebackup\cashbook_%dt%.sql >>"%MyLogFile%"

mysqldump.exe -e -uroot -poctober181986* -hlocalhost bls > C:\webtis\databasebackup\bls.sql
echo.%Date% %Time% Preparing to save the backup >>"%MyLogFile%"
echo.%Date% %Time% Location: C:\webtis\databasebackup >>"%MyLogFile%"
echo.%Date% %Time% Backup has been successfully proccessed with the file name of 'bls_%dt%.sql' >>"%MyLogFile%"
echo.%Date% %Time% Location: C:\webtis\databasebackup\bls_%dt%.sql >>"%MyLogFile%"

echo.%Date% %Time% Ended... >>"%MyLogFile%"