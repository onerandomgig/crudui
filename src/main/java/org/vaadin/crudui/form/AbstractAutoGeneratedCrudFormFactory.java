package org.vaadin.crudui.form;

import com.vaadin.ui.*;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.shared.util.SharedUtil;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.crudui.crud.CrudOperation;

import java.util.*;

/**
 * @author Alejandro Duarte.
 */
public abstract class AbstractAutoGeneratedCrudFormFactory<T> extends AbstractCrudFormFactory<T> {

    protected Map<CrudOperation, String> buttonCaptions = new HashMap<>();
    protected Map<CrudOperation, Resource> buttonIcons = new HashMap<>();
    protected Map<CrudOperation, Set<String>> buttonStyleNames = new HashMap<>();

    protected String cancelButtonCaption = "Cancel";
    protected String validationErrorMessage = "Please fix the errors and try again";
    protected Class<T> domainType;

    public AbstractAutoGeneratedCrudFormFactory(Class<T> domainType) {
        this.domainType = domainType;

        setButtonCaption(CrudOperation.READ, "Ok");
        setButtonCaption(CrudOperation.ADD, "Add");
        setButtonCaption(CrudOperation.UPDATE, "Update");
        setButtonCaption(CrudOperation.DELETE, "Yes, delete");

        setButtonIcon(CrudOperation.READ, null);
        setButtonIcon(CrudOperation.ADD, FontAwesome.SAVE);
        setButtonIcon(CrudOperation.UPDATE, FontAwesome.SAVE);
        setButtonIcon(CrudOperation.DELETE, FontAwesome.TIMES);

        addButtonStyleName(CrudOperation.READ, null);
        addButtonStyleName(CrudOperation.ADD, ValoTheme.BUTTON_PRIMARY);
        addButtonStyleName(CrudOperation.UPDATE, ValoTheme.BUTTON_PRIMARY);
        addButtonStyleName(CrudOperation.DELETE, ValoTheme.BUTTON_DANGER);

        setVisiblePropertyIds(discoverPropertyIds().toArray());
    }

    public void setButtonCaption(CrudOperation operation, String caption) {
        buttonCaptions.put(operation, caption);
    }

    public void setButtonIcon(CrudOperation operation, Resource icon) {
        buttonIcons.put(operation, icon);
    }

    public void addButtonStyleName(CrudOperation operation, String styleName) {
        buttonStyleNames.putIfAbsent(operation, new HashSet<>());
        buttonStyleNames.get(operation).add(styleName);
    }

    public void setCancelButtonCaption(String cancelButtonCaption) {
        this.cancelButtonCaption = cancelButtonCaption;
    }

    public void setValidationErrorMessage(String validationErrorMessage) {
        this.validationErrorMessage = validationErrorMessage;
    }

    protected List<Object> discoverPropertyIds() {

        BeanItemContainer<T> propertyIdsHelper = new BeanItemContainer<T>(domainType);
        return new ArrayList<>(propertyIdsHelper.getContainerPropertyIds());
    }


    protected List<Field> buildAndBind(CrudOperation operation, T domainObject, boolean readOnly, BeanFieldGroup<T> fieldGroup) {
        ArrayList<Field> fields = new ArrayList<>();
        fieldGroup.setItemDataSource(domainObject);
        CrudFormConfiguration configuration = getConfiguration(operation);

        for (int i = 0; i < configuration.getVisiblePropertyIds().size(); i++) {
            Field<?> field = null;
            Object propertyId = configuration.getVisiblePropertyIds().get(i);

            FieldProvider provider = configuration.getFieldProviders().get(propertyId);
            if (provider != null) {
                field = provider.buildField();
            } else {
                Class<? extends Field> fieldType = configuration.getFieldTypes().get(propertyId);
                if (fieldType == null) {
                    fieldType = Field.class;
                }
                field = fieldGroup.buildAndBind(null, propertyId, fieldType);
            }

            if (!configuration.getFieldCaptions().isEmpty()) {
                field.setCaption(configuration.getFieldCaptions().get(i));
            } else {
                field.setCaption(SharedUtil.propertyIdToHumanFriendly(propertyId));
            }

            setDefaultConfiguration(field);
            fieldGroup.bind(field, propertyId);
            field.setReadOnly(readOnly);

            if (!configuration.getDisabledPropertyIds().isEmpty()) {
                field.setEnabled(!configuration.getDisabledPropertyIds().contains(propertyId));
            }

            FieldCreationListener creationListener = configuration.getFieldCreationListeners().get(propertyId);
            if (creationListener != null) {
                creationListener.fieldCreated(field);
            }

            fields.add(field);
        }

        if (!fields.isEmpty() && !readOnly) {
            fields.get(0).focus();
        }

        return fields;
    }

    protected void setDefaultConfiguration(Field<?> field) {
        if (field != null) {
            field.setWidth("100%");
            if (AbstractTextField.class.isAssignableFrom(field.getClass())) {
                ((AbstractTextField) field).setNullRepresentation("");
            }
        }
    }

    protected Button buildOperationButton(CrudOperation operation, T domainObject, BeanFieldGroup fieldGroup, Button.ClickListener clickListener) {
        if (clickListener == null) {
            return null;
        }

        Button button = new Button(buttonCaptions.get(operation), buttonIcons.get(operation));
        buttonStyleNames.get(operation).forEach(styleName -> button.addStyleName(styleName));
        button.addClickListener(event -> {
            try {
                fieldGroup.commit();
                clickListener.buttonClick(event);

            } catch (FieldGroup.CommitException exception) {
                Notification.show(validationErrorMessage);
            }
        });
        return button;
    }

    protected Button buildCancelButton(Button.ClickListener clickListener) {
        if (clickListener == null) {
            return null;
        }

        return new Button(cancelButtonCaption, clickListener);
    }

    protected Layout buildFooter(CrudOperation operation, T domainObject, Button.ClickListener cancelButtonClickListener, Button.ClickListener operationButtonClickListener, BeanFieldGroup fieldGroup) {
        Button operationButton = buildOperationButton(operation, domainObject, fieldGroup, operationButtonClickListener);
        Button cancelButton = buildCancelButton(cancelButtonClickListener);

        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setSizeUndefined();
        footerLayout.setSpacing(true);

        if (cancelButton != null) {
            footerLayout.addComponent(cancelButton);
        }

        if (operationButton != null) {
            footerLayout.addComponent(operationButton);
        }

        return footerLayout;
    }

}
