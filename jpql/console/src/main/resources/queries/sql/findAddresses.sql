select a.address_id, a.address, a.address2, a.district, a.city_id, a.postal_code, a.phone,
       c.city, c.country_id, co.country
  from Address a
  join City c on (a.city_id = c.city_id)
  join Country co on (c.country_id = co.country_id)