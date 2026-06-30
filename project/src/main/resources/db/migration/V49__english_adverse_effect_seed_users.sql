UPDATE users
SET username = 'doctor1',
    email = 'doctor1@iis.com',
    first_name = 'Doctor',
    last_name = 'One'
WHERE username = ('lek' || 'ar1')
  AND NOT EXISTS (
    SELECT 1
    FROM users
    WHERE username = 'doctor1'
       OR email = 'doctor1@iis.com'
  );

UPDATE users
SET username = 'patient1',
    email = 'patient1@iis.com',
    first_name = 'Patient',
    last_name = 'One'
WHERE username = ('pac' || 'ijent1')
  AND NOT EXISTS (
    SELECT 1
    FROM users
    WHERE username = 'patient1'
       OR email = 'patient1@iis.com'
  );

UPDATE users
SET username = 'pharmacovigilance1',
    email = 'pharmacovigilance1@iis.com',
    first_name = 'Safety',
    last_name = 'Reviewer'
WHERE username = ('farmako' || 'vigilant1')
  AND NOT EXISTS (
    SELECT 1
    FROM users
    WHERE username = 'pharmacovigilance1'
       OR email = 'pharmacovigilance1@iis.com'
  );
