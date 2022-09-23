-- Table: sbitestkit.run_status



CREATE TABLE IF NOT EXISTS sbitestkit.run_status
(
    run_id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    status character varying(36) COLLATE pg_catalog."default",
    profile character varying COLLATE pg_catalog."default",
    name character varying COLLATE pg_catalog."default",
    owner character varying(36) COLLATE pg_catalog."default" NOT NULL,
    cr_by character varying(256) COLLATE pg_catalog."default" NOT NULL,
    cr_dtimes timestamp without time zone NOT NULL,
    upd_by character varying(256) COLLATE pg_catalog."default",
    upd_dtimes timestamp without time zone,
    is_deleted boolean,
    del_dtimes timestamp without time zone,
    CONSTRAINT pk_runsta_id PRIMARY KEY (run_id)
)

TABLESPACE pg_default;

ALTER TABLE sbitestkit.run_status
    OWNER to sysadmin;



GRANT ALL ON TABLE sbitestkit.run_status TO sysadmin;

COMMENT ON TABLE sbitestkit.run_status
    IS 'Run Status : MDS Validation kit run status details';

COMMENT ON COLUMN sbitestkit.run_status.run_id
    IS 'Run ID: Unique run id to identify the run';

COMMENT ON COLUMN sbitestkit.run_status.status
    IS 'Status : Run status';

COMMENT ON COLUMN sbitestkit.run_status.profile
    IS 'Profile: Profile of the validation kit run';

COMMENT ON COLUMN sbitestkit.run_status.name
    IS 'Name: Run name';

COMMENT ON COLUMN sbitestkit.run_status.owner
    IS 'Owner: Run owner';

COMMENT ON COLUMN sbitestkit.run_status.cr_by
    IS 'Created By : ID or name of the user who create / insert record';

COMMENT ON COLUMN sbitestkit.run_status.cr_dtimes
    IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';

COMMENT ON COLUMN sbitestkit.run_status.upd_by
    IS 'Updated By : ID or name of the user who update the record with new values';

COMMENT ON COLUMN sbitestkit.run_status.upd_dtimes
    IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';

COMMENT ON COLUMN sbitestkit.run_status.is_deleted
    IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';

COMMENT ON COLUMN sbitestkit.run_status.del_dtimes
    IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';