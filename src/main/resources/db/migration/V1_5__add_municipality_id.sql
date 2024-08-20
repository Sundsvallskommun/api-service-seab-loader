ALTER TABLE IF EXISTS invoice
    ADD COLUMN municipality_id VARCHAR(4);

CREATE INDEX invoice_municipality_id_index
   ON invoice (municipality_id);