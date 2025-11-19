create table event_publication (
	id uuid not null,
	completion_attempts integer not null,
	completion_date timestamp(6) with time zone,
	event_type varchar(255),
	last_resubmission_date timestamp(6) with time zone,
	listener_id varchar(255),
	publication_date timestamp(6) with time zone,
	serialized_event varchar(255),
	status enum ('COMPLETED','FAILED','PROCESSING','PUBLISHED','RESUBMITTED'),
	primary key (id)
);