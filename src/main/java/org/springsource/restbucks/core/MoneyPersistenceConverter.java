package org.springsource.restbucks.core;

import java.util.Locale;

import javax.money.MonetaryAmount;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.javamoney.moneta.Money;

@Converter(autoApply = true)
public class MoneyPersistenceConverter implements AttributeConverter<MonetaryAmount, String> {

    private static final MonetaryAmountFormat FORMAT = MonetaryFormats.getAmountFormat(Locale.ROOT);

    @Override
    public String convertToDatabaseColumn(MonetaryAmount attribute) {
        return attribute == null ? null : FORMAT.format(attribute);
    }

    @Override
    public MonetaryAmount convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Money.parse(dbData, FORMAT);
    }
}
