package javax.xml.bind.annotation;

import javax.xml.bind.JAXBContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Instructs JAXB to also bind other classes when binding this class.
 *
 * <p>
 * Java makes it impractical/impossible to list all sub-classes of
 * a given class. This often gets in a way of JAXB users, as it JAXB
 * cannot automatically list up the classes that need to be known
 * to {@link JAXBContext}.
 *
 * <p>
 * For example, with the following class definitions:
 *
 * <pre>
 * class Animal {}
 * class Dog extends Animal {}
 * class Cat extends Animal {}
 * </pre>
 *
 * <p>
 * The user would be required to create {@link JAXBContext} as
 * <tt>JAXBContext.newInstance(Dog.class,Cat.class)</tt>
 * (<tt>Animal</tt> will be automatically picked up since <tt>Dog</tt>
 * and <tt>Cat</tt> refers to it.)
 *
 * <p>
 * {@link XmlSeeAlso} annotation would allow you to write:
 * <pre>
 * &#64;XmlSeeAlso({Dog.class,Cat.class})
 * class Animal {}
 * class Dog extends Animal {}
 * class Cat extends Animal {}
 * </pre>
 *
 * <p>
 * This would allow you to do <tt>JAXBContext.newInstance(Animal.class)</tt>.
 * By the help of this annotation, JAXB implementations will be able to
 * correctly bind <tt>Dog</tt> and <tt>Cat</tt>. 
 *
 * @author Kohsuke Kawaguchi
 * @since JAXB2.1
 * @version $Revision: 1.1 $
 */
@Target({ElementType.TYPE})
@Retention(RUNTIME)
public @interface XmlSeeAlso {
    Class[] value();
}