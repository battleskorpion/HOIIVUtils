CREATE TEMP TABLE temp_table AS SELECT rowid AS new_id
                                  FROM modifiers
                                 ORDER BY rowid;-- Update the original table with the new IDs.

UPDATE modifiers
   SET id = (
           SELECT new_id
             FROM temp_table
            WHERE rowid = modifiers.rowid
       );-- Drop the temporary table.

DROP TABLE temp_table;
