-- La moneda pasa a ser una propiedad del grupo (antes solo la recordaba el cliente).
-- Los grupos existentes heredan la moneda de su primer gasto; si no tienen gastos, PEN.
ALTER TABLE grupos ADD COLUMN moneda VARCHAR(3);

UPDATE grupos g
SET moneda = COALESCE(
    (SELECT ga.moneda FROM gastos ga WHERE ga.grupo_id = g.id ORDER BY ga.fecha ASC LIMIT 1),
    'PEN'
);

ALTER TABLE grupos ALTER COLUMN moneda SET NOT NULL;
ALTER TABLE grupos ALTER COLUMN moneda SET DEFAULT 'PEN';
