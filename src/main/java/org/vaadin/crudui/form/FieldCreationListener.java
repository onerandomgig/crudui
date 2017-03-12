package org.vaadin.crudui.form;

import java.io.Serializable;

/**
 * @author Alejandro Duarte.
 */
@FunctionalInterface
public interface FieldCreationListener extends Serializable {

    void fieldCreated(Field field);

}
