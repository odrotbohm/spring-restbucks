#!/bin/bash

##### MAKE PAYMENTS

pending_orders_json=$(http ":8080/orders/search/findByStatus?status=PAYMENT_EXPECTED")
pending_order_links=($(echo "$pending_orders_json" | jq -r '._embedded["restbucks:orders"][]._links.self.href'))

echo "Orders pending payment: ${#pending_order_links[@]}"

# Print the URLs to verify they are stored in the array
declare -a payment_links
for order_link in "${pending_order_links[@]}"; do
  payment_links+=($(http "${order_link}" | jq -r '._links["restbucks:payment"].href'))
  echo "Got payment link ${#payment_links[@]}"
done

num_attempts=0
num_payments=0
for payment_link in "${payment_links[@]}"; do
  echo $payment_link
  status=$(echo "{ \"number\": \"1234123412341234\" }" | http PUT ${payment_link} --print=h | grep HTTP | awk '{print $2}')
  ((num_attempts++))
  if [ $status == "201" ]; then
    ((num_payments++))
  fi
  echo -n "Payment $num_attempts [$status " && ([ "$status" -eq 201 ] && echo "OK]" || echo "ERROR]")
done

if [[ $num_payments == ${#pending_order_links[@]} ]]; then
  echo "SUCCESS: Submitted payments for all $num_payments pending orders"
else
  echo "ERROR!! Submitted payments for $num_payments of ${#pending_order_links[@]} pending orders"
fi
