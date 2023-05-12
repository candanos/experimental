      *> Sample GnuCOBOL program
       identification division.
       program-id. SUBPGM02.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
         01 V-PN      PIC X(10) VALUE 'SUBPGM01->'. 
       LINKAGE SECTION.
        COPY SUBPRM02.
       procedure division using SUBPRM02.
       DISPLAY V-PN 'THIS IS SUBPGM02'
       display V-PN SUBPRM02
        
       goback.
       