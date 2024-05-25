#!/bin/bash

# Assign default value of 1 if input is empty
num_orders=${1:-1}
echo "Number of orders requested: $num_orders"

##### CREATE ORDERS

# Prepare order request
drink=$(http ":8080/drinks/search/findByName?name=Cappuchino" | jq ._links.self.href)
location="To go"
order="{ \"drinks\": [${drink}], \"location\": \"${location}\" }"

# Place order(s)
counter=1
while [ $counter -le $num_orders ]; do
  status=$(echo "${order}" | http --headers http://localhost:8080/orders --print h | grep HTTP | awk '{print $2}')
  echo -n "Order $counter [$status " && ([ "$status" -eq 201 ] && echo "OK]" || echo "ERROR]")
  ((counter++))
done

echo "Finished placing orders."