/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.generator.exception;

/**
 * Exception for unconfigured generators.
 * @author Javier Sanz-Cruzado Puig
 */
public class GeneratorNotConfiguredException extends Exception
{
    /**
     * Constructs a TwitterCollectorException with the given detail message.
     * @param message The detail message of the TwitterCollectorException.
     */
    public GeneratorNotConfiguredException(String message) 
    {
        super(message);
    }

    /**
     * Constructs a TwitterCollectorException with the given root cause.
     * @param cause The root cause of the TwitterCollectorException.
     */
    public GeneratorNotConfiguredException(Throwable cause) 
    {
        super(cause);
    }

    /**
     * Constructs a TwitterCollectorException with the given detail message and root cause.
     * @param message The detail message of the TwitterCollectorException.
     * @param cause The root cause of the TwitterCollectorException.
     */
    public GeneratorNotConfiguredException(String message, Throwable cause) 
    {
        super(message, cause);
    }
}
