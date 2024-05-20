#!/bin/bash

##### CREATE ORDERS
# Creates orders for each location for each drink and for combinations of 2 drinks.

# Define locations
locations=("To go" "In store")

error_flag=false

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
  drink_names+=("$(echo "${drinks_http_response}" | jq -r --argjson idx "$index" '._embedded.drinks[$idx].prompt')")

  ((index++))
done

num_locations=${#locations[@]}
num_drinks=${#drinks[@]}
expected_num_orders=$((num_drinks * (num_drinks + 1) / 2 * num_locations))
echo "Generating orders based on $num_locations locations and $num_drinks drinks"

# Place orders
declare -a payment_links

# Outer loop: locations
num_orders=0
location_index=0
echo "Order counters: [order:location:drinks]"
for location in "${locations[@]}"; do
  ((location_index++))
  drinks_index=0

  # Order all single drink combinations
  for ((i = 0; i < ${#drinks[@]}; i++)); do
    # Order all single drink combinations
    ((drinks_index++))
    ((num_orders++))
    drink_order="${drinks[i]}"
    echo "Order $num_orders:$location_index:$drinks_index - $location ${drink_names[i]}"
    order=$(echo "{ \"drinks\": [${drink_order}], \"location\": \"${location}\" }" | http http://localhost:8080/orders)
    payment_links+=($(echo ${order} | jq -r '._links["restbucks:payment"].href'))
  done

  # Order all two drink combinations
  for ((i = 0; i < ${#drinks[@]} - 1; i++)); do
    for ((j = i + 1; j < ${#drinks[@]}; j++)); do
      ((drinks_index++))
      ((num_orders++))
      drink_order="${drinks[i]},${drinks[j]}"
      echo "Order $num_orders:$location_index:$drinks_index - $location ${drink_names[i]}, ${drink_names[j]}"
      order=$(echo "{ \"drinks\": [${drink_order}], \"location\": \"${location}\" }" | http http://localhost:8080/orders)
      payment_links+=($(echo ${order} | jq -r '._links["restbucks:payment"].href'))
    done
  done

done

if [ "$num_orders" -ne "$expected_num_orders" ]; then
  error_flag=true
  echo "ERROR: Expected to generate $expected_num_orders, but generated $num_orders"
else
  echo "Generated $num_orders orders"
fi

##### MAKE PAYMENTS

echo "Submitting payments"
creditCardNumber=""
expectedResult=""
num_payments_submitted=0
num_payments_accepted=0
for payment_link in "${payment_links[@]}"; do
  # Conditional logic to set the credit card number based on the random number
  random_number=$(( RANDOM % 10 ))        # Generate a random number between 0 and 99
  if [ "$random_number" -lt 8 ]; then
      creditCardNumber="1234123412341234" # 80% of the time, card is valid
      expectedResult="201"
  elif [ "$random_number" -lt 9 ]; then
      creditCardNumber="1111222233334444" # 10% of the time, card not found in db
      expectedResult="500"
  else
      creditCardNumber="abcdefghijklmnop" # 10% of the time, invalid format
      expectedResult="400"
  fi

  status_code=$(echo "{ \"number\": \"${creditCardNumber}\" }" | http PUT ${payment_link} --print=h | grep HTTP | awk '{print $2}')
  if [ $status_code == $expectedResult ]; then
    ((num_payments_submitted++))
    echo "OK: HTTP status $status_code for card $creditCardNumber at $payment_link"
    if [ $status_code == "201" ]; then
      ((num_payments_accepted++))
    fi
  else
    error_flag=true
    echo "ERROR: Expected HTTP status $expectedResult, but got $status_code for card $creditCardNumber at $payment_link"
  fi
done
echo "$num_payments_accepted of $num_payments_submitted payments passed credit card validation"

echo
if [[ $error_flag == "true" ]]; then
  echo "ERROR!! Expected to process $num_orders payments, but processed $num_payments_submitted"
else
  echo "SUCCESS: Expected and actual results match"
fi
