LOCK TABLES `GLOBAL_EVENT_ATTR` WRITE;

INSERT INTO `global_event_attr` (ID, LABEL, EVENT_ATTR_TYPE_ID, VERSION) VALUES
(1, "PBMC Count (x10^6)", 1, 0),
(3, "Consent",            5, 0),
(4, "Phlebotomist",       2, 0),
(6, "Biopsy Length",      1, 0),
(7, "Patient Type",       4, 0);

UNLOCK TABLES;
