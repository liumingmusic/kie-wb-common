/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.services.backend.validation.asset;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.guvnor.common.services.project.builder.service.BuildService;
import org.guvnor.common.services.shared.message.Level;
import org.guvnor.common.services.shared.validation.model.ValidationMessage;
import org.guvnor.test.TestFileSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ValidatorTest {

    @Mock
    Path path;

    private TestFileSystem testFileSystem;

    private Validator validator;

    @Before
    public void setUp() throws Exception {
        testFileSystem = new TestFileSystem();
        validator = new Validator( projectService(), buildService() );
    }

    @After
    public void tearDown() throws Exception {
        testFileSystem.tearDown();
    }

    @Test
    public void testValidateWithAValidDRLFile() throws Throwable {
        final Path path = path( "/GuvnorM2RepoDependencyExample1/src/main/resources/rule2.drl" );
        final String content = "package org.kie.workbench.common.services.builder.tests.test1\n" +
                "\n" +
                "rule R2\n" +
                "when\n" +
                "Bean()\n" +
                "then\n" +
                "end";

        List<ValidationMessage> errors = validator.validate( path, new ByteArrayInputStream( content.getBytes() ) );

        assertTrue( errors.isEmpty() );
    }

    @Test
    public void testValidateWithAInvalidDRLFile() throws Throwable {
        final Path path = path( "/GuvnorM2RepoDependencyExample1/src/main/resources/rule2.drl" );
        final String content = "package org.kie.workbench.common.services.builder.tests.test1\n" +
                "\n" +
                "rule R2\n" +
                "when\n" +
                "Ban()\n" +
                "then\n" +
                "end";

        List<ValidationMessage> errors = validator.validate( path, new ByteArrayInputStream( content.getBytes() ) );

        assertFalse( errors.isEmpty() );
    }

    @Test
    public void testValidateWithAValidJavaFile() throws Throwable {
        final Path path1 = path( "/GuvnorM2RepoDependencyExample1/src/main/java/org/kie/workbench/common/services/builder/tests/test1/Bean.java" );
        final String content = "package org.kie.workbench.common.services.builder.tests.test1;\n" +
                "\n" +
                "public class Bean {\n" +
                "    private final int value;\n" +
                "\n" +
                "    public Bean(int value) {\n" +
                "        this.value = value;\n" +
                "    }\n" +
                "\n" +
                "}";

        List<ValidationMessage> validate = validator.validate( path1, new ByteArrayInputStream( content.getBytes() ) );

        assertTrue( validate.isEmpty() );
    }

    @Test
    public void testValidateWithAInalidJavaFile() throws Throwable {
        final Path path1 = path( "/GuvnorM2RepoDependencyExample1/src/main/java/org/kie/workbench/common/services/builder/tests/test1/Bean.java" );
        final String content = "package org.kie.workbench.common.services.builder.tests.test1;\n" +
                "\n" +
                "public class Bean {\n" +
                "    private fnal int value;\n" +
                "\n" +
                "}\n";

        List<ValidationMessage> validate = validator.validate( path1, new ByteArrayInputStream( content.getBytes() ) );

        assertFalse( validate.isEmpty() );
    }

    @Test
    public void testValidateWhenTheresNoProject() throws Exception {
        Path path = path( "/META-INF/beans.xml" );
        InputStream inputStream = this.getClass().getResourceAsStream( "/META-INF/beans.xml" );

        List<ValidationMessage> errors = validator.validate( path, inputStream );

        assertTrue( errors.isEmpty() );
    }

    @Test
    public void testFilterMessageWhenMessageIsInvalid() throws Throwable {
        Path path = path( "/GuvnorM2RepoDependencyExample1/src/main/resources/rule2.drl" );
        ValidationMessage errorMessage = errorMessage( path( "/GuvnorM2RepoDependencyExample1/src/main/resources/rule1.drl" ) );

        List<ValidationMessage> result = applyPredicate( errorMessage, validator.fromValidatedPath( path ) );

        assertTrue( result.isEmpty() );
    }

    @Test
    public void testFilterMessageWhenMessageIsValid() throws Throwable {
        Path path = path( "/GuvnorM2RepoDependencyExample1/src/main/resources/rule2.drl" );
        ValidationMessage errorMessage = errorMessage( path );

        List<ValidationMessage> result = applyPredicate( errorMessage, validator.fromValidatedPath( path ) );

        assertFalse( result.isEmpty() );
    }

    @Test
    public void testFilterMessageWhenMessageIsBlank() throws Throwable {
        Path path = path( "/GuvnorM2RepoDependencyExample1/src/main/resources/rule2.drl" );
        ValidationMessage errorMessage = errorMessage( null );

        List<ValidationMessage> result = applyPredicate( errorMessage, validator.fromValidatedPath( path ) );

        assertFalse( result.isEmpty() );
    }

    private List<ValidationMessage> applyPredicate( final ValidationMessage errorMessage,
                                                    final Predicate<ValidationMessage> predicate ) {
        return validationMessages( errorMessage )
                .stream()
                .filter( predicate )
                .collect( Collectors.toList() );
    }

    private ArrayList<ValidationMessage> validationMessages( final ValidationMessage errorMessage ) {
        return new ArrayList<ValidationMessage>() {{
            add( errorMessage );
        }};
    }

    private ValidationMessage errorMessage( Path path ) {
        return new ValidationMessage( 0, Level.ERROR, path, 0, 0, null );
    }

    private Path path( final String resourceName ) throws URISyntaxException {
        final URL urlToValidate = this.getClass().getResource( resourceName );
        return Paths.convert( testFileSystem.fileSystemProvider.getPath( urlToValidate.toURI() ) );
    }

    private BuildService buildService() {
        return testFileSystem.getReference( BuildService.class );
    }

    private KieProjectService projectService() {
        return testFileSystem.getReference( KieProjectService.class );
    }

    private IOService ioService() {
        return testFileSystem.getReference( IOService.class );
    }
}