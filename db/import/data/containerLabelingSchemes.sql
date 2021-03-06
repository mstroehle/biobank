LOCK TABLES `CONTAINER_LABELING_SCHEME` WRITE;
INSERT INTO `CONTAINER_LABELING_SCHEME` (ID, NAME, MIN_CHARS, MAX_CHARS, MAX_ROWS, MAX_COLS, MAX_CAPACITY, HAS_MULTIPLE_LAYOUT, VERSION) VALUES
( 1, "SBS Standard",           2, 3, 16,   24,   384, 0, 0),
( 2, "CBSR 2 char alphabetic", 2, 2, null, null, 576, 1, 0),
( 3, "2 char numeric",         2, 2, null, null, 99,  1, 0),
( 4, "Dewar",                  2, 2, 2,    2,    4,   0, 0),
( 5, "CBSR SBS",               2, 2, 9,    9,    81,  0, 0),
( 6, "2 char alphabetic",      2, 2, null, null, 676, 1, 0),
( 7, "Box 85 by 2",            2, 3, 85,   2,    170, 0, 0);
UNLOCK TABLES;
