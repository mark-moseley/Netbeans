/*
 * NewEntity.java
 *
 * Created on July 20, 2006, 4:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hints;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Entity class NewEntity
 * 
 * @author jindra
 */
@Entity
class MakePublic implements Serializable{
    @Id
    private Long id;
}