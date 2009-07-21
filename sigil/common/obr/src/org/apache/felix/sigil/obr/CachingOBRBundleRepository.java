/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.felix.sigil.obr;


import java.io.File;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.felix.sigil.model.eclipse.ISigilBundle;
import org.apache.felix.sigil.repository.IRepositoryVisitor;


public class CachingOBRBundleRepository extends AbstractOBRBundleRepository
{

    private SoftReference<List<ISigilBundle>> bundles;


    public CachingOBRBundleRepository( String id, URL repositoryURL, File obrCache, File bundleCache, long updatePeriod )
    {
        super( id, repositoryURL, obrCache, bundleCache, updatePeriod );
    }


    @Override
    public void accept( IRepositoryVisitor visitor, int options )
    {
        for ( ISigilBundle b : loadFromCache( options ) )
        {
            if ( !visitor.visit( b ) )
            {
                break;
            }
        }
    }


    public synchronized void refresh()
    {
        super.refresh();
        if ( bundles != null )
        {
            bundles.clear();
            notifyChange();
        }
    }


    private synchronized List<ISigilBundle> loadFromCache( int options )
    {
        List<ISigilBundle> cached = bundles == null ? null : bundles.get();
        if ( cached == null )
        {
            try
            {
                final LinkedList<ISigilBundle> read = new LinkedList<ISigilBundle>();
                readBundles( new OBRListener()
                {
                    public void handleBundle( ISigilBundle bundle )
                    {
                        read.add( bundle );
                    }
                } );
                cached = read;
                bundles = new SoftReference<List<ISigilBundle>>( cached );
            }
            catch ( Exception e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return cached;
    }
}
