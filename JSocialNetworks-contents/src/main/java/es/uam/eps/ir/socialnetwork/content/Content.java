/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.content;

/**
 * User-created content.
 * @author Javier Sanz-Cruzado Puig
 * @param <T> Type of the content identifier
 * @param <C> Type of the content
 */
public class Content<T,C> 
{
    /**
     * Content identifier
     */
    private final T contentId;
    /**
     * Content text.
     */
    private final C content;
    /**
     * Content timestamp
     */
    private final long timestamp;

    /**
     * Constructor.
     * @param contentId the identifier
     * @param content the text representation of the content
     * @param timestamp the creation timestamp.
     */
    public Content(T contentId, C content, long timestamp) 
    {
        this.contentId = contentId;
        this.content = content;
        this.timestamp = timestamp;
    }

    /**
     * Gets the identifier of the content
     * @return the content identifier.
     */
    public T getContentId() 
    {
        return contentId;
    }

    /**
     * Gets the text representation of the content.
     * @return the text representation of the content.
     */
    public C getContent() 
    {
        return content;
    }

    /**
     * Gets the timestamp of the content.
     * @return the timestamp of the content.
     */
    public long getTimestamp() 
    {
        return timestamp;
    }
    
    
}
