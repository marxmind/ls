set dt=%date:~7,2%-%date:~4,2%-%date:~10,4%_%time:~0,2%_%time:~3,2%_%time:~6,2%

echo Backup database.....
echo %dt%
C:
cd C:\Program Files\MariaDB 11.1\bin 

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

mysqldump.exe -e -uroot -poctober181986* -hlocalhost bank_cheque > C:\webtis\databasebackup\bank_cheque.sql
mysqldump.exe -e -uroot -poctober181986* -hlocalhost bris_poblacion > C:\webtis\databasebackup\bris_poblacion.sql
mysqldump.exe -e -uroot -poctober181986* -hlocalhost brisid > C:\webtis\databasebackup\brisid.sql
mysqldump.exe -e -uroot -poctober181986* -hlocalhost cashbook > C:\webtis\databasebackup\cashbook.sql
mysqldump.exe -e -uroot -poctober181986* -hlocalhost gstrax > C:\webtis\databasebackup\gstrax.sql
mysqldump.exe -e -uroot -poctober181986* -hlocalhost ibuy > C:\webtis\databasebackup\ibuy.sql
mysqldump.exe -e -uroot -poctober181986* -hlocalhost taxation > C:\webtis\databasebackup\taxation.sql
mysqldump.exe -e -uroot -poctober181986* -hlocalhost tdh > C:\webtis\databasebackup\tdh.sql
mysqldump.exe -e -uroot -poctober181986* -hlocalhost webtis > C:\webtis\databasebackup\webtis.sql

::mysqldump.exe -e -uroot -poctober181986* -hlocalhost bank_cheque > C:\webtis\databasebackup\bank_cheque2023.sql
::mysqldump.exe -e -uroot -poctober181986* -hlocalhost bank_cheque2 > C:\webtis\databasebackup\bank_cheque22023.sql
::mysqldump.exe -e -uroot -poctober181986* -hlocalhost bls > C:\webtis\databasebackup\bls2023.sql
::mysqldump.exe -e -uroot -poctober181986* -hlocalhost bls2 > C:\webtis\databasebackup\bls22023.sql
::mysqldump.exe -e -uroot -poctober181986* -hlocalhost bris > C:\webtis\databasebackup\bris2023.sql
::mysqldump.exe -e -uroot -poctober181986* -hlocalhost bris_poblacion > C:\webtis\databasebackup\bris_poblacion2023.sql
::mysqldump.exe -e -uroot -poctober181986* -hlocalhost brisid > C:\webtis\databasebackup\brisid2023.sql
::mysqldump.exe -e -uroot -poctober181986* -hlocalhost cashbook > C:\webtis\databasebackup\cashbook2023.sql
::mysqldump.exe -e -uroot -poctober181986* -hlocalhost cashbook2 > C:\webtis\databasebackup\cashbook22023.sql
::mysqldump.exe -e -uroot -poctober181986* -hlocalhost coop > C:\webtis\databasebackup\coop2023.sql
::mysqldump.exe -e -uroot -poctober181986* -hlocalhost gstrax > C:\webtis\databasebackup\gstrax2023.sql
::mysqldump.exe -e -uroot -poctober181986* -hlocalhost ibuy > C:\webtis\databasebackup\ibuy2023.sql
::mysqldump.exe -e -uroot -poctober181986* -hlocalhost poblacioncustomer > C:\webtis\databasebackup\poblacioncustomer2023.sql
::mysqldump.exe -e -uroot -poctober181986* -hlocalhost taxation > C:\webtis\databasebackup\taxation2023.sql
::mysqldump.exe -e -uroot -poctober181986* -hlocalhost taxation2 > C:\webtis\databasebackup\taxation22023.sql
::mysqldump.exe -e -uroot -poctober181986* -hlocalhost tdh > C:\webtis\databasebackup\tdh2023.sql
::mysqldump.exe -e -uroot -poctober181986* -hlocalhost webtis > C:\webtis\databasebackup\webtis2023.sql
::mysqldump.exe -e -uroot -poctober181986* -hlocalhost webtis2 > C:\webtis\databasebackup\webtis22023.sql

echo.%Date% %Time% Ended... >>"%MyLogFile%"