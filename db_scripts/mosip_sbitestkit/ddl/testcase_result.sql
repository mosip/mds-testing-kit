-- Table: sbitestkit.testcase_result

-- DROP TABLE sbitestkit.testcase_result;

CREATE TABLE IF NOT EXISTS sbitestkit.testcase_result
(
    run_id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    testcase_name character varying(64) COLLATE pg_catalog."default" NOT NULL,
    description character varying(128) COLLATE pg_catalog."default",
    owner character varying(36) COLLATE pg_catalog."default" NOT NULL,
    request character varying COLLATE pg_catalog."default",
    response character varying COLLATE pg_catalog."default",
    validation_results character varying COLLATE pg_catalog."default",
    passed boolean,
    device_info character varying COLLATE pg_catalog."default",
    executed_on timestamp without time zone,
    current_status character varying(36) COLLATE pg_catalog."default",
    cr_by character varying(256) COLLATE pg_catalog."default" NOT NULL,
    cr_dtimes timestamp without time zone NOT NULL,
    upd_by character varying(256) COLLATE pg_catalog."default",
    upd_dtimes timestamp without time zone,
    is_deleted boolean,
    del_dtimes timestamp without time zone,
    CONSTRAINT pk_testr_id PRIMARY KEY (run_id, testcase_name)
)

TABLESPACE pg_default;

ALTER TABLE sbitestkit.testcase_result
    OWNER to sysadmin;



GRANT ALL ON TABLE sbitestkit.testcase_result TO sysadmin;

COMMENT ON TABLE sbitestkit.testcase_result
    IS 'Test Case Results: Results captured as part of the test';

COMMENT ON COLUMN sbitestkit.testcase_result.run_id
    IS 'Run ID: Unique run id to identify the run';

COMMENT ON COLUMN sbitestkit.testcase_result.testcase_name
    IS 'Test case Name: Name of the test case';

COMMENT ON COLUMN sbitestkit.testcase_result.description
    IS 'Description : Description of test results';

COMMENT ON COLUMN sbitestkit.testcase_result.owner
    IS 'Owner: Run owner';

COMMENT ON COLUMN sbitestkit.testcase_result.request
    IS 'Request: Test Request';

COMMENT ON COLUMN sbitestkit.testcase_result.response
    IS 'Response: Test Response';

COMMENT ON COLUMN sbitestkit.testcase_result.validation_results
    IS 'Validation Results: Results of the MDS validations';

COMMENT ON COLUMN sbitestkit.testcase_result.passed
    IS 'Passed: Boolean value of the test';

COMMENT ON COLUMN sbitestkit.testcase_result.device_info
    IS 'Device Info: Device Information';

COMMENT ON COLUMN sbitestkit.testcase_result.executed_on
    IS 'Executed On: Test executed date and time';

COMMENT ON COLUMN sbitestkit.testcase_result.current_status
    IS 'Current Status : Current status of the run';

COMMENT ON COLUMN sbitestkit.testcase_result.cr_by
    IS 'Created By : ID or name of the user who create / insert record';

COMMENT ON COLUMN sbitestkit.testcase_result.cr_dtimes
    IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';

COMMENT ON COLUMN sbitestkit.testcase_result.upd_by
    IS 'Updated By : ID or name of the user who update the record with new values';

COMMENT ON COLUMN sbitestkit.testcase_result.upd_dtimes
    IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';

COMMENT ON COLUMN sbitestkit.testcase_result.is_deleted
    IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';

COMMENT ON COLUMN sbitestkit.testcase_result.del_dtimes
    IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';