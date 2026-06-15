package com.akarengin.pulseforge;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
    packages = "com.akarengin.pulseforge",
    importOptions = ImportOption.DoNotIncludeTests.class
)
public class ArchitectureBoundaryTest {

    private static final String BASE = "com.akarengin.pulseforge";

    // -------------------------------------------------------------------------
    // COMMON — no upstream domain dependencies
    // common is infrastructure. It must not know any domain module exists.
    // -------------------------------------------------------------------------

    @ArchTest
    static final ArchRule common_must_not_depend_on_any_domain_module =
        noClasses().that().resideInAPackage(BASE + ".common..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                BASE + ".user..",
                BASE + ".workspace..",
                BASE + ".project..",
                BASE + ".ingestion..",
                BASE + ".processing.."
            )
            .because("common is shared infrastructure — it must have no knowledge of domain modules");

    // -------------------------------------------------------------------------
    // USER — foundational domain, no peer dependencies
    // user owns credential management and user lifecycle.
    // It does not depend on any other domain module.
    // -------------------------------------------------------------------------

    @ArchTest
    static final ArchRule user_must_not_depend_on_peer_domain_modules =
        noClasses().that().resideInAPackage(BASE + ".user..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                BASE + ".workspace..",
                BASE + ".project..",
                BASE + ".ingestion..",
                BASE + ".processing.."
            )
            .because("user is a foundational module — it must not depend on peer domains");

    // -------------------------------------------------------------------------
    // WORKSPACE — depends on common and user service only
    // workspace may call user.service to resolve users.
    // It must not reach into user.repository directly (bypasses user's API).
    // It must not depend on project, ingestion, or processing.
    // -------------------------------------------------------------------------

    @ArchTest
    static final ArchRule workspace_must_not_import_user_repository =
        noClasses().that().resideInAPackage(BASE + ".workspace..")
            .should().dependOnClassesThat()
            .resideInAPackage(BASE + ".user.repository..")
            .because("workspace must call user.service — not reach into user.repository directly");

    @ArchTest
    static final ArchRule workspace_must_not_depend_on_downstream_modules =
        noClasses().that().resideInAPackage(BASE + ".workspace..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                BASE + ".project..",
                BASE + ".ingestion..",
                BASE + ".processing.."
            )
            .because("workspace must not depend on project, ingestion, or processing");

    // -------------------------------------------------------------------------
    // PROJECT — depends on common, user service, workspace service only
    // project may call user.service and workspace.service.
    // It must not reach into their repositories directly.
    // It must not depend on ingestion or processing.
    // -------------------------------------------------------------------------

    @ArchTest
    static final ArchRule project_must_not_import_foreign_repositories =
        noClasses().that().resideInAPackage(BASE + ".project..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                BASE + ".user.repository..",
                BASE + ".workspace.repository.."
            )
            .because("project must call service APIs — not reach into foreign repositories");

    @ArchTest
    static final ArchRule project_must_not_depend_on_downstream_modules =
        noClasses().that().resideInAPackage(BASE + ".project..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                BASE + ".ingestion..",
                BASE + ".processing.."
            )
            .because("project must not depend on ingestion or processing");

    // -------------------------------------------------------------------------
    // INGESTION — depends on common, workspace service, project service only
    // ingestion is the HTTP write path. It validates workspace and project
    // identity via their service APIs, then publishes to RabbitMQ.
    // It must not reach into foreign repositories.
    // It must not depend on processing (RabbitMQ is their only contract).
    // -------------------------------------------------------------------------

    @ArchTest
    static final ArchRule ingestion_must_not_import_foreign_repositories =
        noClasses().that().resideInAPackage(BASE + ".ingestion..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                BASE + ".user.repository..",
                BASE + ".workspace.repository..",
                BASE + ".project.repository.."
            )
            .because("ingestion must call service APIs — not reach into foreign repositories");

    @ArchTest
    static final ArchRule ingestion_must_not_depend_on_processing =
        noClasses().that().resideInAPackage(BASE + ".ingestion..")
            .should().dependOnClassesThat()
            .resideInAPackage(BASE + ".processing..")
            .because("ingestion and processing are decoupled by RabbitMQ — no direct Java dependency allowed");

    // -------------------------------------------------------------------------
    // PROCESSING — to be filled in later
    //
    // Rules to add once processing/ exists:
    //
    // 1. processing must not depend on ingestion.controller or ingestion.service
    //    noClasses().that().resideInAPackage(BASE + ".processing..")
    //        .should().dependOnClassesThat()
    //        .resideInAnyPackage(
    //            BASE + ".ingestion.controller..",
    //            BASE + ".ingestion.service.."
    //        )
    //        .because("processing communicates with ingestion via RabbitMQ only");
    //
    // 2. processing must not depend on workspace or project repositories
    //    noClasses().that().resideInAPackage(BASE + ".processing..")
    //        .should().dependOnClassesThat()
    //        .resideInAnyPackage(
    //            BASE + ".workspace.repository..",
    //            BASE + ".project.repository.."
    //        )
    //        .because("processing must call service APIs — not reach into foreign repositories");
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // ANALYTICS — to be filled in later
    // -------------------------------------------------------------------------
}