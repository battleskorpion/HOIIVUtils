UPDATE effects
   SET 
       supported_scopes = LOWER(supported_scopes),
       supported_targets = LOWER(supported_targets) 
 WHERE (supported_targets IN ('none', 'THIS, ROOT, PREV, FROM, OWNER, CONTROLLER, OCCUPIED, CAPITAL', 'any', 'THIS', 'THIS, ROOT, PREV, FROM') OR 
        supported_scopes IN ('COUNTRY', 'STATE, COUNTRY', 'COUNTRY, CHARACTER', 'CHARACTER', 'STATE', 'STATE, COUNTRY, CHARACTER', 'STRATEGIC_REGION', 'any') );
