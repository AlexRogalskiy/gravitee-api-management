/**
 * Gravitee.io Portal Rest API
 * API dedicated to the devportal part of Gravitee
 *
 * The version of the OpenAPI document: 3.15.0-SNAPSHOT
 * Contact: contact@graviteesource.com
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { User } from './user';


export interface Member { 
    /**
     * Unique identifier of a member (user).
     */
    id?: string;
    user?: User;
    /**
     * Creation date and time of the member.
     */
    created_at?: Date;
    /**
     * Last update date and time of the member.
     */
    updated_at?: Date;
    /**
     * Role of the member. (OWNER, USER, ...).
     */
    role?: string;
}

