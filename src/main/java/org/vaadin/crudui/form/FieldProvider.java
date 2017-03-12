package org.vaadin.crudui.form;

import java.io.Serializable;

/**
 * @author Alejandro Duarte.
 */
@FunctionalInterface
public interface FieldProvider extends Serializable {

    Field buildField();

}
