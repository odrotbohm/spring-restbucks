create table "ORDER" (
	id uuid not null,
	location varchar(32),
	ordered_date timestamp(6),
	status varchar(32),
	version bigint,
	primary key (id)
);
create table line_item (
	id uuid not null,
	drink uuid,
	milk varchar(32),
	name varchar(255),
	"ORDER" uuid,
	price varchar(255),
	quantity integer not null,
	size varchar(32),
	"ORDER_KEY" int,
	line_items_order integer,
	primary key (id)
);
alter table if exists line_item 
	add constraint FK1xpumv0elakrwjfp2pbqlkkdh 
	foreign key ("ORDER") 
	references "ORDER";