alter table invoice
add source enum ('IN_EXCHANGE','STRALFORS') not null default 'IN_EXCHANGE';