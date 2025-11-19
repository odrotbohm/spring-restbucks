create table credit_card (
	number varchar(255) not null,
	card_holder_name varchar(255),
	expiry_month varchar(32),
	expiry_year integer,
	primary key (number)
);

create table credit_card_payment (
	id uuid not null,
	"ORDER" uuid,
	payment_date timestamp(6),
	credit_card_number varchar(255),
	primary key (id)
);
