\c mosip_sbitestkit

GRANT CONNECT
   ON DATABASE mosip_sbitestkit
   TO sbitestkituser;
-- ddl-end --

-- object: grant_3543fb6cf7 | type: PERMISSION --

-- object: grant_8e1a2559ed | type: PERMISSION --
GRANT USAGE
   ON SCHEMA sbitestkit
   TO sbitestkituser;
-- ddl-end --

-- object: grant_8e1a2559ed | type: PERMISSION --
GRANT SELECT,INSERT,UPDATE,DELETE,TRUNCATE,REFERENCES
   ON ALL TABLES IN SCHEMA sbitestkit
   TO sbitestkituser;
-- ddl-end --

ALTER DEFAULT PRIVILEGES IN SCHEMA sbitestkit 
	GRANT SELECT,INSERT,UPDATE,DELETE,REFERENCES ON TABLES TO sbitestkituser;