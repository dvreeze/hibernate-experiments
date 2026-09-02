select
    fe1_0.film_id,fe1_0.description,
    fa1_0.film_id,fa1_0.actor_id,
    a1_0.actor_id,a1_0.first_name,a1_0.last_name,a1_0.last_update,
    fa1_0.last_update,
    fc1_0.film_id,fc1_0.category_id,
    c1_0.category_id,c1_0.last_update,c1_0.name,
    fc1_0.last_update,
    fe1_0.language_id,
    l1_0.language_id,l1_0.last_update,l1_0.name,
    fe1_0.last_update,fe1_0.length,
    ol1_0.language_id,ol1_0.last_update,ol1_0.name,
    fe1_0.rating,fe1_0.release_year,fe1_0.rental_duration,fe1_0.rental_rate,
    fe1_0.replacement_cost,fe1_0.title
  from Film fe1_0
  left join Film_Actor fa1_0 on fe1_0.film_id=fa1_0.film_id
  left join Actor a1_0 on a1_0.actor_id=fa1_0.actor_id
  left join Film_Category fc1_0 on fe1_0.film_id=fc1_0.film_id
  left join Category c1_0 on c1_0.category_id=fc1_0.category_id
  join Language l1_0 on l1_0.language_id=fe1_0.language_id
  left join Language ol1_0 on ol1_0.language_id=fe1_0.original_language_id
 order by fe1_0.film_id
