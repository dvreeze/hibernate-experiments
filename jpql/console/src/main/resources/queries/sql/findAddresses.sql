select a.address_id, a.address, a.address2, a.district, a.city_id, a.postal_code, a.phone,
       ct.city, ct.country_id, co.country
  from Address a
  join City ct on (a.city_id = ct.city_id)
  join Country co on (ct.country_id = co.country_id)