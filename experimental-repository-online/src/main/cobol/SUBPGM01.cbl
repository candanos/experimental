      *> Sample GnuCOBOL program
       identification division.
       program-id. SUBPGM01.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       LINKAGE SECTION.
        01 SUBPRM01 pic X(10).
       procedure division using SUBPRM01.
       DISPLAY 'THIS IS SUBPGM01'
       display SUBPRM01
       CALL 'SUBPGM02' USING SUBPRM01
        

       goback.
       