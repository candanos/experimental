      *> Sample GnuCOBOL program
       identification division.
       program-id. EXPBATCH.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
        01 V-PN      PIC X(10) VALUE 'EXPBATCH->'. 
        01 W-MESSAGE PIC X(100).
        COPY SUBPRM01.
       PROCEDURE DIVISION.
          
       MOVE 'THIS IS JOHN RAMBO' TO W-MESSAGE
       MOVE W-MESSAGE TO SUBPRM01
       display V-PN W-MESSAGE
       CALL 'SUBPGM01' USING SUBPRM01
       display V-PN SUBPRM01
       goback.
       