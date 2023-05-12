      *> Sample GnuCOBOL program
       identification division.
       program-id. SUBPGM01.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 V-PN      PIC X(10) VALUE 'SUBPGM01->'. 
       LINKAGE SECTION.
        COPY SUBPRM01.
       procedure division using SUBPRM01.
       display V-PN SUBPRM01
       CALL 'SUBPGM02' USING SUBPRM01
        

       goback.
       