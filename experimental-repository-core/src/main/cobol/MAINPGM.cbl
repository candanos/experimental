      *> Sample GnuCOBOL program
       identification division.
       program-id. MAINPGM.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
        01 W-MESSAGE PIC X(10).
       procedure division.
       MOVE 'JOHN RAMBO' to W-MESSAGE
       display "Hello, new world!"
       CALL 'SUBPGM01' USING W-MESSAGE 
       goback.
       