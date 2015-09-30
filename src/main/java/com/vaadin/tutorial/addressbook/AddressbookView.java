package com.vaadin.tutorial.addressbook;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ShortcutAction;
import com.vaadin.tutorial.addressbook.backend.Contact;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

/*
 * Extend your design class and add the needed handlers for user interaction.
 * Use BeanFieldGroup to bind data fields from DTO to UI fields. Similarly named
 * field by naming convention or customized with @PropertyId annotation.
 */
public class AddressbookView extends AddressbookDesign {
    Contact contact;

    // Easily bind forms to beans and manage validation and buffering
    BeanFieldGroup<Contact> formFieldBindings;

    /*
     * Hundreds of widgets. Vaadin's user interface components are just Java
     * objects that encapsulate and handle cross-browser support and
     * client-server communication. The default Vaadin components are in the
     * com.vaadin.ui package and there are over 500 more in
     * vaadin.com/directory.
     */

    public AddressbookView() {
        configureComponents();
    }

    /*
     * Synchronous event handling.
     *
     * Receive user interaction events on the server-side. This allows you to
     * synchronously handle those events. Vaadin automatically sends only the
     * needed changes to the web page without loading a new page.
     */
    private void configureComponents() {
        contactForm.setVisible(false);
        save.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        // Bind event handlers to declarative UI with static typing
        save.addClickListener(this::save);
        cancel.addClickListener(this::cancel);
        newContact.addClickListener(e -> edit(new Contact()));

        filter.setInputPrompt("Filter contacts...");
        filter.addTextChangeListener(e -> refreshContacts(e.getText()));

        contactList
                .setContainerDataSource(new BeanItemContainer<>(Contact.class));
        contactList.setColumnOrder("firstName", "lastName", "email");
        contactList.removeColumn("id");
        contactList.removeColumn("birthDate");
        contactList.removeColumn("phone");
        contactList.setSelectionMode(Grid.SelectionMode.SINGLE);
        contactList.addSelectionListener(
                e -> edit((Contact) contactList.getSelectedRow()));
        refreshContacts();
    }

    /*
     * Use any JVM language.
     *
     * Vaadin supports all languages supported by Java Virtual Machine 1.6+.
     * This allows you to program user interface in Java 8, Scala, Groovy or any
     * other language you choose. The new languages give you very powerful tools
     * for organizing your code as you choose. For example, you can implement
     * the listener methods in your compositions or in separate controller
     * classes and receive to various Vaadin component events, like button
     * clicks. Or keep it simple and compact with Lambda expressions.
     */
    private void save(Button.ClickEvent event) {
        try {
            // Commit the fields from UI to DAO
            formFieldBindings.commit();

            // Save DAO to backend with direct synchronous service API
            AddressbookUI.getContactService().save(contact);

            String msg = String.format("Saved '%s %s'.", contact.getFirstName(),
                    contact.getLastName());
            Notification.show(msg, Type.TRAY_NOTIFICATION);
            refreshContacts();
        } catch (FieldGroup.CommitException e) {
            // Validation exceptions could be shown here
        }
    }

    private void cancel(Button.ClickEvent event) {
        // Place to call business logic.
        Notification.show("Cancelled", Type.TRAY_NOTIFICATION);
        contactList.select(null);
    }

    private void edit(Contact contact) {
        this.contact = contact;
        if (contact != null) {
            // Bind the properties of the contact POJO to fiels in this form
            formFieldBindings = BeanFieldGroup.bindFieldsBuffered(contact,
                    this);
            firstName.focus();
        }
        contactForm.setVisible(contact != null);
    }

    private void refreshContacts() {
        refreshContacts(filter.getValue());
    }

    private void refreshContacts(String stringFilter) {
        contactList.setContainerDataSource(new BeanItemContainer<>(
                Contact.class,
                AddressbookUI.getContactService().findAll(stringFilter)));
        contactForm.setVisible(false);
    }

}
