#!/bin/bash

##### CREATE ORDERS
# Creates orders for each location for each drink and for combinations of 2 drinks.

# Define locations
locations=("To go" "In store")

# Get drink options
drinks_http_response=$(http :8080/drinks/by-name)
declare -a drinks
declare -a drink_names
index=0
while true; do
  value=$(echo "${drinks_http_response}" | jq --argjson idx "$index" '._embedded.drinks[$idx].value')
  # If the value is null, exit the loop
  if [ -z "$value" ] || [ "$value" = "null" ]; then break; fi
  drinks+=($(echo "${drinks_http_response}" | jq --argjson idx "$index" '._embedded.drinks[$idx].value'))
  drink_names+=($(echo "${drinks_http_response}" | jq -r --argjson idx "$index" '._embedded.drinks[$idx].prompt'))
  ((index++))
done

num_locations=${#locations[@]}
num_drinks=${#drinks[@]}
expected_num_orders=$((num_drinks * (num_drinks + 1) / 2 * num_locations))
echo "Generating $expected_num_orders orders. Options: $num_locations locations, $num_drinks drinks"

# Place orders
declare -a payment_links

# Outer loop: locations
orders_index=0
location_index=0
for location in "${locations[@]}"; do
  ((location_index++))
  drinks_index=0

  # Order all single drink combinations
  for ((i = 0; i < ${#drinks[@]}; i++)); do
    # Order all single drink combinations
    ((drinks_index++))
    ((orders_index++))
    drink_order="${drinks[i]}"
    echo "Loop $orders_index:$location_index:$drinks_index - $location ${drink_names[i]}"
    order=$(echo "{ \"drinks\": [${drink_order}], \"location\": \"${location}\" }" | http http://localhost:8080/orders)
    payment_links+=($(echo ${order} | jq -r '._links["restbucks:payment"].href'))
  done

  # Order all two drink combinations
  for ((i = 0; i < ${#drinks[@]} - 1; i++)); do
    for ((j = i + 1; j < ${#drinks[@]}; j++)); do
      ((drinks_index++))
      ((orders_index++))
      drink_order="${drinks[i]},${drinks[j]}"
      echo "Loop $orders_index:$location_index:$drinks_index - $location ${drink_names[i]}, ${drink_names[j]}"
      order=$(echo "{ \"drinks\": [${drink_order}], \"location\": \"${location}\" }" | http http://localhost:8080/orders)
      payment_links+=($(echo ${order} | jq -r '._links["restbucks:payment"].href'))
    done
  done

done

if [ "$orders_index" -ne "$expected_num_orders" ]; then
  echo "WARN: Generated $orders_index orders. Expected to generate $expected_num_orders."
else
  echo "Generated $orders_index orders."
fi

##### MAKE PAYMENTS

echo "Processing ${#payment_links[@]} orders"
creditCardNumber=""
for payment_link in "${payment_links[@]}"; do
  # Conditional logic to set the credit card number based on the random number
  random_number=$(( RANDOM % 10 ))       # Generate a random number between 0 and 99
  if [ "$random_number" -lt 8 ]; then
      creditCardNumber="1234123412341234"     # 80% of the time, card is valid
  elif [ "$random_number" -lt 9 ]; then
      creditCardNumber="1111222233334444" # 10% of the time, card not found in db
  else
      creditCardNumber="abcdefghijklmnop" # 10% of the time, invalid format
  fi

  status_code=$(echo "{ \"number\": \"${creditCardNumber}\" }" | http PUT ${payment_link} --print=h | grep HTTP | awk '{print $2}')
  echo "Got HTTP status $status_code using card $creditCardNumber at $payment_link"
done
