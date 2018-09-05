/*
 * Copyright 2018 Rundeck, Inc. (http://rundeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rundeck.services

import com.dtolabs.rundeck.core.common.Framework
import com.dtolabs.rundeck.core.plugins.PluginMetadata
import com.dtolabs.rundeck.core.plugins.ServiceProviderLoader
import com.dtolabs.rundeck.core.plugins.configuration.Description
import com.dtolabs.rundeck.core.plugins.configuration.Property
import com.dtolabs.rundeck.plugins.util.PropertyBuilder
import grails.testing.services.ServiceUnitTest
import net.sf.cglib.core.Local
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.WebUtils
import spock.lang.Specification

import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.text.SimpleDateFormat


class PluginApiServiceSpec extends Specification implements ServiceUnitTest<PluginApiService> {
    String fakePluginId = "Fake Plugin".encodeAsSHA256().substring(0,12)

    void "list plugins"() {
        given:
        messageSource.addMessage("framework.service.Notification.description",Locale.ENGLISH,"Triggered when a Job starts, succeeds, or fails.")
        def fwksvc = Mock(FrameworkService)
        def fwk = Mock(Framework)
        fwksvc.getRundeckFramework() >> fwk
        fwk.getPluginManager() >> Mock(ServiceProviderLoader)
        service.frameworkService = fwksvc

        def pluginDescs = [
                "Notification": [new FakePluginDescription()]
        ]
        def pluginData = [
                descriptions        : pluginDescs,
                serviceDefaultScopes: [],
                bundledPlugins      : [],
                embeddedFilenames   : [],
                specialConfiguration: [],
                specialScoping      : [],
                uiPluginProfiles    : []
        ]
        def fakeMeta = new FakePluginMetadata()

        service.metaClass.listPluginsDetailed = { -> pluginData }
        service.metaClass.getLocale = { -> Locale.ENGLISH }

        when:
        1 * service.frameworkService.rundeckFramework.pluginManager.getPluginMetadata(_,_) >> fakeMeta
        def response = service.listPlugins()
        def service = response[0]
        def entry = service.providers[0]

        then:
        response.size() == 1
        service.service == "Notification"
        service.desc == "Triggered when a Job starts, succeeds, or fails."
        service.providers.size() == 1
        entry.artifactId == fakePluginId
        entry.name == "fake"
        entry.title == "Fake Plugin"
        entry.description == "This is the best fake plugin"
        entry.builtin == false
        entry.pluginVersion == "1.0"
        entry.pluginDate == 1534253342000
        entry.enabled == true

    }

    class FakePluginMetadata implements PluginMetadata {

        @Override
        String getFilename() {
            return null
        }

        @Override
        File getFile() {
            return null
        }

        @Override
        String getPluginArtifactName() {
            return "Fake Plugin"
        }

        @Override
        String getPluginAuthor() {
            return null
        }

        @Override
        String getPluginFileVersion() {
            return "1.0"
        }

        @Override
        String getPluginVersion() {
            return "1.0"
        }

        @Override
        String getPluginUrl() {
            return null
        }

        @Override
        Date getPluginDate() {
            return  new SimpleDateFormat("EEE MMM dd hh:mm:ss Z yyyy").parse("Tue Aug 14 08:29:02 CDT 2018")
        }

        @Override
        Date getDateLoaded() {
            return new Date()
        }

    }


    class FakePluginDescription implements Description {

        @Override
        String getName() {
            return "fake"
        }

        @Override
        String getTitle() {
            return "Fake Plugin"
        }

        @Override
        String getDescription() {
            return "This is the best fake plugin"
        }

        @Override
        List<Property> getProperties() {
            def p1 = PropertyBuilder.builder()
                                    .name("prop1")
                                    .title("Property 1")
                                    .description("A fake property for the fake plugin")
                                    .required(true)
                                    .defaultValue("alpha")
                                    .values("alpha","beta","gamma")
                                    .type(Property.Type.Select)
                                    .build()
            def p2 = PropertyBuilder.builder()
                                    .name("password")
                                    .title("Password")
                                    .description("The password to the fake plugin")
                                    .required(false)
                                    .type(Property.Type.String)
                                    .renderingAsPassword()
                                    .build()

            return [ p1, p2]
        }

        @Override
        Map<String, String> getPropertiesMapping() {
            return [:]
        }

        @Override
        Map<String, String> getFwkPropertiesMapping() {
            return [:]
        }
    }
}