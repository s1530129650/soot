/* Soot - a J*va Optimization Framework
 * Copyright (C) 2004 Ondrej Lhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package soot;
import java.util.*;
import soot.util.*;

/** Representation of a reference to a field as it appears in a class file.
 * Note that the field directly referred to may not actually exist; the
 * actual target of the reference is determined according to the resolution
 * procedure in the Java Virtual Machine Specification, 2nd ed, section 5.4.3.2.
 */

class AbstractSootFieldRef implements SootFieldRef {
    public AbstractSootFieldRef( 
            SootClass declaringClass,
            String name,
            Type type ) {
        this.declaringClass = declaringClass;
        this.name = name;
        this.type = type;
    }

    private final SootClass declaringClass;
    private final String name;
    private final Type type;

    public SootClass declaringClass() { return declaringClass; }
    public String name() { return name; }
    public Type type() { return type; }

    public String getSignature() {
        return SootField.getSignature(declaringClass, name, type);
    }

    public class FieldResolutionFailedException extends ResolutionFailedException {
        public FieldResolutionFailedException() {
            super("Class "+declaringClass+" doesn't have field "+name+
                    " : "+type+
                    "; failed to resolve in superclasses and interfaces" );
        }
        public String toString() {
            StringBuffer ret = new StringBuffer();
            resolve(ret);
            ret.append(super.toString());
            return ret.toString();
        }
    }

    public SootField resolve() {
        return resolve(null);
    }
    private SootField resolve(StringBuffer trace) {
        SootField ret = null;
        SootClass cl = declaringClass;
        while(true) {
            if(trace != null) trace.append(
                    "Looking in "+cl+" which has fields "+cl.getFields()+"\n" );
            if( cl.declaresField(name, type) ) {
                return cl.getField(name, type);
            }

            if(Scene.v().allowsPhantomRefs() && cl.isPhantom())
            {
                SootField f = new SootField(name, type);
                f.setPhantom(true);
                cl.addField(f);
                return f;
            } else {
                LinkedList queue = new LinkedList();
                queue.addAll( cl.getInterfaces() );
                while( !queue.isEmpty() ) {
                    SootClass iface = (SootClass) queue.removeFirst();
                    if(trace != null) trace.append(
                            "Looking in "+iface+" which has fields "+iface.getFields()+"\n" );
                    if( iface.declaresField(name, type) ) {
                        return iface.getField( name, type );
                    }
                    queue.addAll( iface.getInterfaces() );
                }
                if( cl.hasSuperclass() ) cl = cl.getSuperclass();
                else break;
            }
        }
        throw new FieldResolutionFailedException();
    }
}
