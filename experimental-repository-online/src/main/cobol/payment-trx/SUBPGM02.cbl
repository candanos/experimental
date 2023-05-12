      *> Sample GnuCOBOL program
       identification division.
       program-id. SUBPGM02.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       LINKAGE SECTION.
        01 SUBPRM02 pic X(10).
       procedure division using SUBPRM02.
       DISPLAY 'THIS IS SUBPGM02'
       display SUBPRM02
        
       goback.
       