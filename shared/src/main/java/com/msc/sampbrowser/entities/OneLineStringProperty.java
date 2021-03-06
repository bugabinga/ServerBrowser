package com.msc.sampbrowser.entities;

import java.util.Objects;

import javafx.beans.property.SimpleStringProperty;

/**
 * Special StringProperty Class, that automatically removes all LineBreaks of the content and trims
 * it.
 *
 * @author Marcel
 * @since 25.06.2017
 */
public class OneLineStringProperty extends SimpleStringProperty
{
	public OneLineStringProperty()
	{
		super();
	}

	public OneLineStringProperty(final String initialValue)
	{
		super(replaceLineBreaks(initialValue));
	}

	@Override
	public void setValue(final String value)
	{
		final String toSet = replaceLineBreaks(value);
		super.setValue(toSet);
	}

	@Override
	public void set(final String value)
	{
		final String toSet = replaceLineBreaks(value);
		super.set(toSet);
	}

	private static String replaceLineBreaks(final String value)
	{
		/*
		 * Replacing \r\n first and afterwards leftover \n by doing it like that, i never replace a
		 * single line break by two spaces
		 */
		final String toSet = Objects.isNull(value) ? null : value.replace("\r\n", " ").replace("\n", " ").trim();
		return toSet;
	}
}
